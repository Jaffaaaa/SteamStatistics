package xyz.steamstatistics.commands.cmd;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;

import java.util.HashMap;

public class BotStatsCommand extends Command {

    private Core core;
    public BotStatsCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        int serverCount = core.jda.getGuilds().size();
        int userCount = 0;
        for (Guild g : core.jda.getGuilds()) {
            userCount = userCount + g.getMemberCount();
        }

        event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", [Server Count: " + serverCount + "] [User Count: " + userCount + "]").queue();
        return true;
    }

    @Override
    public String icon() {
        return "-";
    }

    @Override
    public String subCmd() {
        return "botstats";
    }

    @Override
    public String help() {
        return "-";
    }

    @Override
    public String usage() {
        return ".s botstats";
    }

    @Override
    public String description() {
        return "-";
    }

    @Override
    public boolean isAdminCmd() {
        return true;
    }

    @Override
    public int cooldown() {
        return 5;
    }

    private HashMap<Long, Long> cooldownMap = new HashMap<>();

    @Override
    public HashMap<Long, Long> cooldownMap() {
        return cooldownMap;
    }

}
