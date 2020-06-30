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

public class RustStatsCommand extends Command {

    private Core core;
    public RustStatsCommand(Core core) {
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
                String link = "http://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=252490&key=" + core.steamKey + "&steamid=" + steamId;
                JSONObject obj = JsonReader.readJsonFromUrl(link);
                JSONObject playerStats = (JSONObject) obj.get("playerstats");
                stats = (JSONArray) playerStats.get("stats");
            } catch (IOException e) {
                channel.sendMessage("This user has their game details set to private.").queue();
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
            DecimalFormat formatterTwo = new DecimalFormat("#.##");

            // Get data
            int deaths = 0;
            int playerkills = 0;
            int suicides = 0;
            int bulletHit = 1;
            int hs = 0;
            double hsPercentage = 0.0;
            double kd = 0.0;
            try {
                deaths = Integer.parseInt(map.get("deaths"));
                playerkills = Integer.parseInt(map.get("kill_player"));
                suicides = Integer.parseInt(map.get("death_suicide"));
                bulletHit = Integer.parseInt(map.get("bullet_hit_player"));
                hs = Integer.parseInt(map.get("headshot"));
                hsPercentage = (double) hs / (double) bulletHit;
                kd = Double.parseDouble(formatterTwo.format((double) playerkills / (double) deaths));
            } catch (Exception ignored) {}

            String steamName = SteamFunc.getSteamName(steamId);

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor("[Rust Data] " + steamName, "https://steamcommunity.com/profiles/" + steamId, "https://data.apksum.com/78/game.LRTH.RustBuilder.Free/1.0.4/icon.png")
                    .setColor(Color.black)
                    .setDescription("**Profile Link: **https://steamcommunity.com/profiles/" + steamId + "\nShowing Rust data for player: **" + steamName + "**")
                    .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png")
                    .addField(":anger: Player Kills", formatter.format(playerkills), true)
                    .addField(":skull_crossbones: Total Deaths", formatter.format(deaths), true)
                    .addField(":gun: Suicides", formatter.format(suicides), true)
                    .addField(":head_bandage: Headshots", formatter.format(hs), true)
                    .addField(":hotsprings: Player HS %", formatterTwo.format(hsPercentage), true)
                    .addField(":clap: Player K/D", kd + "", true)
                    ;

            channel.sendMessage(eb.build()).queue();
            return true;
        } catch (Exception e) {
            channel.sendMessage("An exception occurred while trying to read data. Try again later.").queue();
        }
        return false;
    }

    @Override
    public String icon() {
        return ":hammer_pick:";
    }

    @Override
    public String subCmd() {
        return "ruststats";
    }

    @Override
    public String help() {
        return "Command returns data about the player in regards to Rust. \n**Usage: "
                + core.PREFIX + " " + usage() + "**\nIf the bot isn't directing to the correct user, use a steam link instead.";
    }

    @Override
    public String usage() {
        return "ruststats <steam name/link>";
    }

    @Override
    public String description() {
        return "Returns Rust stats about a player.";
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
