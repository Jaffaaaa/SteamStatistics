package xyz.steamstatistics.commands.cmd;

import me.ugurcan.steamapi.Game;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;
import xyz.steamstatistics.misc.Logger;
import xyz.steamstatistics.misc.SteamFunc;

import java.awt.*;
import java.util.HashMap;

public class GameLookupCommand extends Command {

    private Core core;
    public GameLookupCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        if (args == null || args[0].equals("") || args[0].equals(" ")) {
            channel.sendMessage(event.getAuthor().getAsMention() + ", you did not provide a valid game name/link.").queue();
        } else {
            Game g = core.steamAPI.getGameFromArgs(args, channel);

            if (g == null) {
                channel.sendMessage("We could not collect data on this game. Try again later.").queue();
                return false;
            }

            Logger.log("Data collected for \"" + g.getTitle() + "\".", this.getClass());
            String[] countData = SteamFunc.getPlayerCount(Integer.parseInt(g.getId()));

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor("[Game Info] " + g.getTitle(), "https://store.steampowered.com/app/" + g.getId(), g.getThumbnailURL())
                    .setColor(Color.ORANGE)
                    .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png")
                    .setDescription(g.getDescription())
                    .addField(":moneybag:  Price:", g.getPrice(), true)
                    .addField(":information_source:  Game ID:", g.getId(), true)
                    .addField(":clock5:  Release:", g.getReleaseDate(), true)
                    .addField(":chart_with_upwards_trend:  Player Count:", countData[0] + "", true)
                    .addField(":chart_with_upwards_trend:  24-hour Peak:", countData[1], true)
                    .addField(":chart_with_upwards_trend:  All Time Peak:", countData[2], true);

            channel.sendMessage(embed.build()).queue();
            return true;
        }
        return false;
    }

    @Override
    public String icon() {
        return ":magnet:";
    }

    @Override
    public String subCmd() {
        return "gamelookup";
    }

    @Override
    public String help() {
        return "Looks up general data about any game.\n**Usage: *" + usage() + "**";
    }

    @Override
    public String usage() {
        return subCmd() + " <game name/link>";
    }

    @Override
    public String description() {
        return "Looks up general data about any game.";
    }

    @Override
    public int cooldown() {
        return 15;
    }

    private HashMap<Long, Long> cooldownMap = new HashMap<>();

    @Override
    public HashMap<Long, Long> cooldownMap() {
        return cooldownMap;
    }

    @Override
    public boolean isAdminCmd() {
        return false;
    }
}
