package xyz.steamstatistics.data;

import java.util.HashMap;

public class GameShort {

    public static HashMap<String, String> map;

    public static boolean contains(String shortened) {
        return map.containsKey(shortened.toLowerCase());
    }

    public static String getTitle(String shortened) {
        return map.get(shortened.toLowerCase());
    }

    public static void apply() {
        map = new HashMap<>();
        map.put("csgo", "Counter-Strike: Global Offensive");
        map.put("cs", "Counter-Strike");
        map.put("css", "Counter-Strike: Source");
        map.put("r6", "Tom Clancy's Rainbow Six® Siege");
        map.put("rainbow 6", "Tom Clancy's Rainbow Six® Siege");
        map.put("rainbow six", "Tom Clancy's Rainbow Six® Siege");
        map.put("pubg", "PLAYERUNKNOWN'S BATTLEGROUNDS");
        map.put("gta", "Grand Theft Auto V");
        map.put("gta v", "Grand Theft Auto V");
        map.put("gtav", "Grand Theft Auto V");
        map.put("gta5", "Grand Theft Auto V");
        map.put("gta 5", "Grand Theft Auto V");
        map.put("tf2", "Team Fortress 2");
        map.put("rdr", "Red Dead Redemption 2");
    }

}
