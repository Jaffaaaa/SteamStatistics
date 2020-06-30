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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TF2StatsCommand extends Command {

    private Core core;
    public TF2StatsCommand(Core core) {
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

        try {
            JSONArray stats;
            try {
                String link = "http://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=440&key=" + core.steamKey + "&steamid=" + steamId;
                JSONObject obj = JsonReader.readJsonFromUrl(link);
                JSONObject playerStats = (JSONObject) obj.get("playerstats");
                stats = (JSONArray) playerStats.get("stats");
            } catch (IOException e) {
                channel.sendMessage("This user has their game details set to private.").queue();
                e.printStackTrace();
                return false;
            }

            ArrayList<String> list = new ArrayList<String>();
            if (stats != null) {
                int len = stats.length();
                for (int i = 0; i < len; i++) {
                    list.add(stats.get(i).toString());
                }
            }

            Map<String, String> map = new HashMap<>();
            for (String str : list) {
                String[] arr = str.split("\"");
                String name = arr[3];
                String value = arr[6].substring(1, arr[6].length() - 1);
                map.put(name, value);
            }

            DecimalFormat formatter = new DecimalFormat("#,###");

            int totalKills = 0;
            int totalAssists = 0;
            int totalDamageDealt = 0;
            int totalPointsScored = 0;
            int totalPointsCaptured = 0;
            int totalPointsDefenses = 0;
            for (String className : classes) {
                totalKills = totalKills + Integer.parseInt(map.get(className + ".accum.iNumberOfKills"));
                totalAssists = totalAssists + Integer.parseInt(map.get(className + ".accum.iKillAssists"));
                totalDamageDealt = totalDamageDealt + Integer.parseInt(map.get(className + ".accum.iDamageDealt"));
                totalPointsScored = totalPointsScored + Integer.parseInt(map.get(className + ".accum.iPointsScored"));
                totalPointsCaptured = totalPointsCaptured + Integer.parseInt(map.get(className + ".accum.iPointCaptures"));
                totalPointsDefenses = totalPointsDefenses + Integer.parseInt(map.get(className + ".accum.iPointDefenses"));
            }

            String steamName = SteamFunc.getSteamName(steamId);

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor("[TF2 Data] " + steamName, "https://steamcommunity.com/profiles/" + steamId, "https://pbs.twimg.com/profile_images/1013352784626900992/xYqlaU9y_400x400.jpg")
                    .setColor(Color.YELLOW)
                    .setDescription("**Profile Link: **https://steamcommunity.com/profiles/" + steamId + "\nShowing TF2 data for player: **" + steamName + "**")
                    .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png")
                    .addField(":anger: Total Kills", formatter.format(totalKills), true)
                    .addField(":syringe: Total Assists", formatter.format(totalAssists), true)
                    .addField(":100: Total Damage Dealt", formatter.format(totalDamageDealt), true)
                    .addField(":bar_chart: Total Points Scored", formatter.format(totalPointsScored), true)
                    .addField(":axe: Control Points Captured", formatter.format(totalPointsCaptured), true)
                    .addField(":warning: Control Points Defenses", formatter.format(totalPointsDefenses), true);

            channel.sendMessage(eb.build()).queue();
            return true;
        } catch (Exception e) {
            channel.sendMessage("An exception occurred while trying to read data. Try again later.").queue();
        }
        return false;
    }

    @Override
    public String icon() {
        return ":hammer:";
    }

    @Override
    public String subCmd() {
        return "tf2stats";
    }

    @Override
    public String help() {
        return "Command returns data about the player in regards to Team Fortress 2. \n**Usage: "
                + core.PREFIX + " " + usage() + "**\nIf the bot isn't directing to the correct user, use a steam link instead.";
    }

    @Override
    public String usage() {
        return "tf2stats <player name/link>";
    }

    @Override
    public String description() {
        return "Returns TF2 stats about a player.";
    }

    @Override
    public boolean isAdminCmd() {
        return false;
    }

    @Override
    public int cooldown() {
        return 15;
    }

    private HashMap<Long, Long> map = new HashMap<>();

    @Override
    public HashMap<Long, Long> cooldownMap() {
        return map;
    }

    public String[] classes = {"Demoman", "Engineer", "Heavy", "Medic", "Pyro", "Scout", "Sniper", "Soldier", "Spy"};
}
