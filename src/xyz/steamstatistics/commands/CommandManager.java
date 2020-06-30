package xyz.steamstatistics.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.RandomStringUtils;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.data.Devs;
import xyz.steamstatistics.misc.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CommandManager extends ListenerAdapter {

    private Core core;
    public CommandManager(Core core) {
        this.core=core;
    }

    public List<Command> commands = new ArrayList<>();

    public CommandManager add(Command command) {
        commands.add(command);
        return this;
    }

    public CommandManager build() {
        core.jda.addEventListener(this);
        return this;
    }

    private int cmdCount = 0;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw() + " ";

        if (!raw.startsWith(core.PREFIX)) {
            return;
        }

        String subCmd = raw.substring(core.PREFIX.length() + 1);
        boolean found = false;
        for (Command cmd : commands) {
            if (subCmd.toLowerCase().startsWith(cmd.subCmd().toLowerCase())) {
                User author = event.getAuthor();
                long id = author.getIdLong();

                if (cmd.isAdminCmd() && !Devs.isDev(id)) {
                    event.getChannel().sendMessage(author.getAsMention() + ", you do not have access to this command.").queue();
                    return;
                }

                if (cmd.cooldownMap().containsKey(id)) {
                    long elapsedTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - cmd.cooldownMap().get(id));
                    if (elapsedTime > cmd.cooldown()) {
                        cmd.cooldownMap().remove(id);
                    } else {
                        long timeLeft = cmd.cooldown() - elapsedTime;
                        event.getChannel().sendMessage("You have **" + timeLeft + "s** until you can use this command again.").queue();
                        return;
                    }
                }

                found = true;

                new Thread(() -> {
                    String cmdId = cmdCount + "";
                    cmdCount++;
                    Logger.log("Command [ID-" + cmdId + "] '" + subCmd.substring(0, subCmd.length()-1) + "' was executed in guild [" + event.getGuild().getName() + " | "
                            + event.getGuild().getId() + "] by user: " + author.getAsTag(), CommandManager.class);
                    long timeOnExecute = System.nanoTime();
                    boolean success = cmd.execute(event, raw, event.getTextChannel(), raw.substring(core.PREFIX.length() + cmd.subCmd().length() + 2).split(" "));
                    DecimalFormat formatter = new DecimalFormat("#.###");
                    double timeSinceExecuted = ((double) System.nanoTime() - (double) timeOnExecute) / 1000000000.0;
                    Logger.log("Command [ID-" + cmdId + "] returned " + String.valueOf(success).toLowerCase() + ". (" + formatter.format(timeSinceExecuted) + "s)", CommandManager.class);
                }).start();

                if (!Devs.isDev(id)) {
                    cmd.cooldownMap().put(id, System.nanoTime());
                }
            }
        }

        if (!found) {
            event.getTextChannel().sendMessage("You provided an unknown command, "
                    + event.getAuthor().getAsMention() + ". Use **"
                    + core.PREFIX + " help** for a list of commands.")
                    .queue();
        }
    }


}
