package xyz.steamstatistics.commands.cmd;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;

import java.awt.*;
import java.util.HashMap;

public class HelpCommand extends Command {

    private Core core;
    public HelpCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        if (args.length == 0 || args[0].equals(" ") || args[0].equals("")) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor("Command List", "https://github.com/Jaffaaaa/SteamStatistics",
                    "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png");
            builder.setDescription("Use ``.s help [command]`` for anymore help on the command.\n*This bot is open-source: https://github.com/Jaffaaaa/SteamStatistics*");
            builder.setColor(Color.red);

            for (Command cmd : core.commandManager.commands) {
                if (cmd.subCmd().equalsIgnoreCase("help"))
                    continue;
                if (cmd.isAdminCmd())
                    continue;

                builder.addField(cmd.icon() + " " + core.PREFIX + " " + cmd.usage(), cmd.description(), true);
            }

            MessageEmbed embed = builder.build();
            channel.sendMessage(embed).queue();
        } else {
            String subCmd = args[0].toLowerCase();

            Command match = null;
            for (Command cmd : core.commandManager.commands) {
                if (subCmd.equalsIgnoreCase(cmd.subCmd().toLowerCase())) {
                    match = cmd;
                }
            }

            if (match == null) {
                channel.sendMessage(event.getAuthor().getAsMention() + ", the command you requested help for doesn't exist. Maybe you spelt it wrong?").queue();
                return false;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor("[Help] " + core.PREFIX + " " + match.subCmd(), "https://github.com/Jaffaaaa/SteamStatistics",
                    "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png");
            builder.setColor(Color.MAGENTA);
            builder.setDescription(match.help());
            channel.sendMessage(builder.build()).queue();
        }
        return true;
    }

    @Override
    public String icon() {
        return "";
    }

    @Override
    public String subCmd() {
        return "help";
    }

    @Override
    public String help() {
        return "Gives a list of commands. If you need more in-depth help on a certain command use: **.ss help [command]**.";
    }

    @Override
    public String usage() {
        return subCmd();
    }

    @Override
    public String description() {
        return "Shows available commands.";
    }

    @Override
    public boolean isAdminCmd() {
        return false;
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
