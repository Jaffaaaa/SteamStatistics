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
import java.util.*;

public class CSGOStatsCommand extends Command {

    private Core core;
    public CSGOStatsCommand(Core core) {
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
                String link = "http://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=730&key=" + core.steamKey + "&steamid=" + steamId;
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
                for (int i=0; i < len; i++){
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

            String totalKills = map.get("total_kills");
            String totalDeaths = map.get("total_deaths");
            String lastMatchOutcome = map.get("last_match_wins");
            if (lastMatchOutcome.equals("16")) { lastMatchOutcome = "Win"; } else { lastMatchOutcome = "Loss"; }
            String lastMatchKills = map.get("last_match_kills");
            String lastMatchDeaths = map.get("last_match_deaths");
            String favouriteWeaponLastMatch = toWeaponName.get(Integer.valueOf(map.get("last_match_favweapon_id")));
            String lastMatchMVPs = map.get("last_match_mvps");
            double kD = Double.parseDouble(totalKills) / Double.parseDouble(totalDeaths);

            DecimalFormat formatter = new DecimalFormat("#,###");
            DecimalFormat formatterTwo = new DecimalFormat("#.##");

            double totalRounds = Integer.parseInt(map.get("last_match_ct_wins")) + Integer.parseInt(map.get("last_match_t_wins"));
            double lastMatchDmg = Double.parseDouble(map.get("last_match_damage"));
            String lastMatchADR = String.valueOf((int) (lastMatchDmg / totalRounds));

            String steamName = SteamFunc.getSteamName(steamId);

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor("[CS:GO Data] " + steamName, "https://steamcommunity.com/profiles/" + steamId, "https://cdn2.iconfinder.com/data/icons/popular-games-1/50/csgo_squircle-512.png")
                    .setColor(Color.orange)
                    .setDescription("**Profile Link: **https://steamcommunity.com/profiles/" + steamId + "\nShowing CS:GO data for player: **" + steamName + "**")
                    .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png")
                    .addField(":anger: Total Kills", formatter.format(Integer.parseInt(totalKills)), true)
                    .addField(":skull_crossbones: Total Deaths", formatter.format(Integer.parseInt(totalDeaths)), true)
                    .addField(":v:  Total K/D", formatterTwo.format(kD), true)
                    .addField(":crown: Last Match Outcome", lastMatchOutcome, true)
                    .addField(":point_right: Last Match Kills", lastMatchKills, true)
                    .addField(":point_left: Last Match Deaths", lastMatchDeaths, true)
                    .addField(":muscle: Last Match ADR", lastMatchADR, true);
                    try {
                        eb.addField(":gun: Last Match Best Weapon", favouriteWeaponLastMatch, true);
                    } catch (IllegalArgumentException ex) {
                        eb.addField(":gun: Last Match Best Weapon", "Unknown", true);
                    }
                    eb.addField(":partying_face: Last Match MVPs", formatter.format(Integer.parseInt(lastMatchMVPs)), true);

            channel.sendMessage(eb.build()).queue();
            return true;
        } catch (Exception e) {
            channel.sendMessage("An exception occurred while trying to read data. Try again later.").queue();
            return false;
        }
    }

    @Override
    public String icon() {
        return ":gun:";
    }

    @Override
    public String subCmd() {
        return "csgostats";
    }

    @Override
    public String help() {
        return "Command returns data about the player in regards to Counter Strike: Global Offensive. \n**Usage: "
                + core.PREFIX + " " + usage() + "**\nIf the bot isn't directing to the correct user, use a steam link instead.";
    }

    @Override
    public String usage() {
        return "csgostats <steam name/link>";
    }

    @Override
    public String description() {
        return "Returns CS:GO stats about a player.";
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

    public static HashMap<Integer, String> toWeaponName = new HashMap<>();

    static {
        toWeaponName.put(1, "Desert Eagle");
        toWeaponName.put(2, "Dual Berettas");
        toWeaponName.put(3, "Five-SeveN");
        toWeaponName.put(4, "Glock-18");
        toWeaponName.put(7, "AK-47");
        toWeaponName.put(8, "AUG");
        toWeaponName.put(9, "AWP");
        toWeaponName.put(10, "FAMAS");
        toWeaponName.put(11, "G35G1");
        toWeaponName.put(13, "Galil AR");
        toWeaponName.put(14, "M249");
        toWeaponName.put(16, "M4A4");
        toWeaponName.put(17, "Mac-10");
        toWeaponName.put(19, "P90");
        toWeaponName.put(23, "MP5-SD");
        toWeaponName.put(24, "UMP-45");
        toWeaponName.put(25, "XM1014");
        toWeaponName.put(26, "PP-Bizon");
        toWeaponName.put(27, "MAG-7");
        toWeaponName.put(28, "Negev");
        toWeaponName.put(29, "Sawed-Off");
        toWeaponName.put(30, "Tec-9");
        toWeaponName.put(31, "Zeus x27");
        toWeaponName.put(32, "P2000");
        toWeaponName.put(33, "MP7");
        toWeaponName.put(34, "MP9");
        toWeaponName.put(35, "Nova");
        toWeaponName.put(36, "P250");
        toWeaponName.put(38, "SCAR-20");
        toWeaponName.put(39, "SG-553");
        toWeaponName.put(40, "SSG 08");
        toWeaponName.put(41, "Knife");
        toWeaponName.put(42, "Knife");
        toWeaponName.put(43, "Flashbang");
        toWeaponName.put(44, "High Explosive Grenade");
        toWeaponName.put(45, "Smoke Grenade");
        toWeaponName.put(46, "Molotov");
        toWeaponName.put(47, "Decoy Grenade");
        toWeaponName.put(48, "Incendiary Grenade");
        toWeaponName.put(59, "Knife");
        toWeaponName.put(60, "M4A1-S");
        toWeaponName.put(61, "USP-S");
        toWeaponName.put(63, "CZ75-Auto");
        toWeaponName.put(64, "Revolver");
        toWeaponName.put(74, "Knife");
        toWeaponName.put(81, "Fire Grenade");
        toWeaponName.put(82, "Decoy Grenade");
        toWeaponName.put(83, "Frag Grenade");
        toWeaponName.put(500, "Bayonet");
        toWeaponName.put(503, "Classic Knife");
        toWeaponName.put(505, "Flip Knife");
        toWeaponName.put(506, "Gut Knife");
        toWeaponName.put(507, "Karambit");
        toWeaponName.put(508, "M9 Bayonet");
        toWeaponName.put(509, "Huntsman Knife");
        toWeaponName.put(512, "Falchion Knife");
        toWeaponName.put(514, "Bowie Knife");
        toWeaponName.put(515, "Butterfly Knife");
        toWeaponName.put(516, "Shadow Daggers");
        toWeaponName.put(517, "Paracord Knife");
        toWeaponName.put(518, "Survival Knife");
        toWeaponName.put(519, "Ursus Knife");
        toWeaponName.put(520, "Navaja Knife");
        toWeaponName.put(521, "Nomad Knife");
        toWeaponName.put(522, "Stilleto Knife");
        toWeaponName.put(523, "Talon Knife");
        toWeaponName.put(525, "Skeleton Knife");
    }
}
