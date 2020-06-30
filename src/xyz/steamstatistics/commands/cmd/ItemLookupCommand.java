package xyz.steamstatistics.commands.cmd;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;

import java.util.HashMap;

public class ItemLookupCommand extends Command {

    private Core core;
    public ItemLookupCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        if (args == null || args[0].equals("") || args[0].equals(" ")) {
            channel.sendMessage(event.getAuthor().getAsMention() + ", you did not provide a valid **item name**.").queue();
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (String str : args) {
            str = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
            sb.append(str).append(" ");
        }
        String itemName = sb.toString();

        channel.sendMessage(itemName).queue();
        return true;
    }

    @Override
    public String icon() {
        return ":watch:";
    }

    @Override
    public String subCmd() {
        return "item";
    }

    @Override
    public String help() {
        return "Command returns data about an item in the steam marketplace. \n**Usage: "
                + core.PREFIX + " " + usage() + "**";
    }

    @Override
    public String usage() {
        return "item <item name>";
    }

    @Override
    public String description() {
        return "Gets information on an item in the Steam Marketplace.";
    }

    @Override
    public boolean isAdminCmd() {
        return false;
    }

    @Override
    public int cooldown() {
        return 15;
    }

    public HashMap<Long, Long> cooldown = new HashMap<>();

    @Override
    public HashMap<Long, Long> cooldownMap() {
        return cooldown;
    }
}
