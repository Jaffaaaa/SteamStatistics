package xyz.steamstatistics.misc;

import me.ugurcan.steamapi.SteamAPI;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xyz.steamstatistics.Core;

import java.io.IOException;
import java.text.NumberFormat;

public class SteamFunc {

    private static Core core;
    public SteamFunc(Core core) {
        SteamFunc.core =core;
    }

    public static String[] getPlayerCount(int gameId) {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://steamcharts.com/app/" + gameId).timeout(SteamAPI.timeout).get();
        } catch (IOException ignored) {}

        if (doc == null) {
            String[] nulls = new String[3];
            int i = 0;
            for (String st : nulls) {
                nulls[i] = "?";
                i++;
            }
            return nulls;
        }

        String[] data = doc.getElementsByClass("app-stat").text().trim().split(" ");
        String[] num = new String[3];
        num[0] = NumberFormat.getInstance().format(Integer.parseInt(data[0]));
        num[1] = NumberFormat.getInstance().format(Integer.parseInt(data[2]));
        num[2] = NumberFormat.getInstance().format(Integer.parseInt(data[5]));
        return num;
    }

    public static String getSteamId(String[] args, TextChannel channel) {
        String profileLink = "";
        if (args[0].contains("http")) {
            profileLink = args[0];
        } else {
            profileLink = "https://steamcommunity.com/id/" + args[0];
        }

        try {
            Document lookupPage = Jsoup.connect("https://steamidfinder.com/lookup/" + profileLink.substring(30)).timeout(SteamAPI.timeout).get();
            String pane = lookupPage.getElementsByClass("panel-body").first().text();
            String[] details = pane.split(" ");
            return details[5];
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSteamName(String steamId) {
        try {
            String link = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + core.steamKey + "&steamids=" + steamId;
            JSONObject page = JsonReader.readJsonFromUrl(link);
            JSONObject player = (JSONObject) page.getJSONObject("response").getJSONArray("players").get(0);
            return player.getString("personaname");
        } catch (Exception e) {
            return steamId;
        }
    }

}
