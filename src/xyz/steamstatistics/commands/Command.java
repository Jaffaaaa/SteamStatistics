package xyz.steamstatistics.commands;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;

public abstract class Command {

    public abstract boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args);
    public abstract String icon();
    public abstract String subCmd();
    public abstract String help();
    public abstract String usage();
    public abstract String description();
    public abstract boolean isAdminCmd();

    public abstract int cooldown();
    public abstract HashMap<Long, Long> cooldownMap();

}
