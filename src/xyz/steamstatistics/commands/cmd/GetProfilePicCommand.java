package xyz.steamstatistics.commands.cmd;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;
import xyz.steamstatistics.misc.JsonReader;
import xyz.steamstatistics.misc.SteamFunc;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class GetProfilePicCommand extends Command {

    private Core core;
    public GetProfilePicCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        if (args == null || args[0].equals("") || args[0].equals(" ")) {
            channel.sendMessage(event.getAuthor().getAsMention() + ", you did not provide a valid username/link.").queue();
            return false;
        }

        String steamId = SteamFunc.getSteamId(args, channel);
        if (steamId == null) {
            channel.sendMessage("Connection timed out, or you asked for an invalid profile. Try again later.").queue();
            return false;
        }

        String steamName = SteamFunc.getSteamName(steamId);

        String link = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + core.steamKey + "&steamids=" + steamId;
        JSONObject obj;
        try {
            obj = JsonReader.readJsonFromUrl(link);
        } catch (IOException e) {
            channel.sendMessage("Unable to read data from this user. Try again later.").queue();
            return false;
        }

        JSONArray arr = (JSONArray) ((JSONObject) obj.get("response")).get("players");
        JSONObject data = arr.getJSONObject(0);
        String img = data.getString("avatarfull");

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("[Profile Picture] " + steamName, "https://steamcommunity.com/profiles/" + steamId, img)
                .setColor(Color.cyan)
                .setImage(img)
                .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png");

        channel.sendMessage(eb.build()).queue();
        return true;
    }

    @Override
    public String icon() {
        return ":guard:";
    }

    @Override
    public String subCmd() {
        return "getpic";
    }

    @Override
    public String help() {
        return "Gets a highest-resolution (184x184) image of a steam user's profile picture." + "\n**Usage: "
                + core.PREFIX + " " + usage() + "**\nIf the bot isn't directing to the correct user, use a steam link instead.";
    }

    @Override
    public String usage() {
        return "getpic <steam name/link>";
    }

    @Override
    public String description() {
        return "Gets profile picture of a steam user.";
    }

    @Override
    public boolean isAdminCmd() {
        return false;
    }

    @Override
    public int cooldown() {
        return 10;
    }

    private HashMap<Long, Long> map = new HashMap<>();

    @Override
    public HashMap<Long, Long> cooldownMap() {
        return map;
    }
}
