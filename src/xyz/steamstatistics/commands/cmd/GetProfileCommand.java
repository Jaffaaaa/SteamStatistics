package xyz.steamstatistics.commands.cmd;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import me.ugurcan.steamapi.SteamAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;
import xyz.steamstatistics.misc.JsonReader;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetProfileCommand extends Command {

    private Core core;
    public GetProfileCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        if (args == null || args[0].equals("") || args[0].equals(" ")) {
            channel.sendMessage(event.getAuthor().getAsMention() + ", you did not provide a valid user link/name.").queue();
            return false;
        }

        channel.sendMessage("Collecting data...").queue();

        String profileLink = "";
        if (args[0].contains("http")) {
            profileLink = args[0];
        } else {
            StringBuilder sb = new StringBuilder();
            for (String str : args) {
                sb.append(str);
            }
            profileLink = "https://steamcommunity.com/id/" + sb.toString();
        }

        Document steamPage;
        Document lookupPage;
        Document recentPage;
        Document sortedPage;
        try {
            WebClient webClient = new WebClient();
            webClient.waitForBackgroundJavaScript(1000);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage steampage = webClient.getPage(profileLink);
            steamPage = Jsoup.parse(steampage.asXml());

            lookupPage = Jsoup.connect("https://steamidfinder.com/lookup/" + profileLink.substring(30)).timeout(SteamAPI.timeout).get();

            HtmlPage recentpage = webClient.getPage(profileLink + "/games/?tab=recent");
            recentPage = Jsoup.parse(recentpage.asXml());

            HtmlPage sortedpage = webClient.getPage(profileLink + "/games/?tab=all&sort=playtime");
            sortedPage = Jsoup.parse(sortedpage.asXml());

        } catch (IOException e) {
            channel.sendMessage("Connection timed out, or you asked for an invalid profile. Try again later.").queue();
            return false;
        }

        /*
        This is probably badly coded. I'll come back to this later
         */
        String name = checkNull(() -> steamPage.getElementsByClass("actual_persona_name").first().text());
        String img = checkNull(() -> steamPage.getElementsByClass("playerAvatarAutoSizeInner").select("img").first().attr("src"));
        String pane = checkNull(() -> lookupPage.getElementsByClass("panel-body").first().text());
        String[] details = pane.split(" ");
        String summery = checkNull(() -> steamPage.getElementsByClass("profile_summary noexpand").first().text().trim());
        String steamLevel = checkNull(() -> steamPage.getElementsByClass("persona_name persona_level").text());
        String loc = checkNull(() -> pane.substring(pane.indexOf("location") + 8, pane.indexOf("Add")));
        String steamID64 = checkNull(() -> details[5]);
        String creation = checkNull(() -> details[15] + " " + details[16] + " " + details[17]);
        String gameCount = checkNull(() -> steamPage.getElementsByClass("profile_item_links").first().getElementsByClass("profile_count_link_total").first().text());
        String groupCount = checkNull(() -> steamPage.getElementsByClass("profile_group_links profile_count_link_preview_ctn responsive_groupfriends_element").first().getElementsByClass("profile_count_link_total").first().text());
        String primaryGroup = checkNull(() -> steamPage.getElementsByClass("profile_group profile_primary_group").first().getElementsByClass("whiteLink").text());
        String mostRecent = checkNull(() -> recentPage.getElementsByClass("gameListRowItemName ellipsis ").first().text().trim());
        String mostPlayed = checkNull(() -> sortedPage.getElementsByClass("gameListRowItemName ellipsis ").first().text().trim());
        String mostPlayedHours = checkNull(() -> sortedPage.getElementsByClass("ellipsis hours_played").first().text().trim());
        String state = checkNull(() -> details[12]);

        if (details[17].equalsIgnoreCase("1970")) {
            creation = "Unknown";
        }

        String vacStatus = "Not Banned";
        String gameBanStatus = "Not Banned";
        String tradeBanStatus = "Not Banned";
        try {
            JSONObject obj = JsonReader.readJsonFromUrl("http://api.steampowered.com/ISteamUser/GetPlayerBans/v1/?key="
                    + core.steamKey + "&steamids=" + steamID64);
            JSONObject user = (JSONObject) ((JSONArray) obj.get("players")).get(0);
            Map map = user.toMap();

            int vacBans = (int) map.get("NumberOfVACBans");
            int gameBans = (int) map.get("NumberOfGameBans");
            String tradeBanned = String.valueOf(map.get("EconomyBan"));

            if (vacBans > 0) {
                vacStatus = "Banned *(" + vacBans + ")*";
            }

            if (gameBans > 0) {
                gameBanStatus = "Banned *(" + gameBans + ")*";
            }

            if (!tradeBanned.equalsIgnoreCase("none")) {
                tradeBanStatus = "Banned";
            }
        } catch (IOException ignored) {}

        if (summery.toCharArray().length > 200) {
            summery = summery.substring(0, 200) + "**...**";
        }

        if (emptyCount > 5) {
            state = "Private";
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("[User] " + name, profileLink, img)
                .setColor(Color.PINK)
                .setDescription("**Profile Link: **" + profileLink + " [" + steamID64 + "]\n'" + summery + "'")
                .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png")
                .addField(":star2: Account Creation", creation, true)
                .addField(":gem: Steam Level", steamLevel, true)
                .addField(":detective: Primary Group", primaryGroup, true)
                .addField(":wind_blowing_face: Steam ID64", steamID64, true)
                .addField(":money_with_wings: Game Count", gameCount, true)
                .addField(":family_man_boy: Group Count", groupCount, true)
                .addField(":homes: Location", loc, true)
                .addField(":hammer: Most Recent Game", mostRecent, true)
                .addField(":revolving_hearts: Most Played Game", mostPlayed + " (" + mostPlayedHours + ")", true)
                .addField(":warning: VAC Standing", vacStatus, true)
                .addField(":warning: Trade Standing", tradeBanStatus, true)
                .addField(":warning: Game Ban Standing", gameBanStatus, true);

        if (emptyCount > 3) {
            eb.setDescription("**Profile Link: **" + profileLink + " [" + steamID64 + "]\n**This profile seems to have a lot of missing data. Private profile?**");
        }

        channel.sendMessage(eb.build()).queue();
        return true;
    }

    @Override
    public String icon() {
        return ":ringed_planet:";
    }

    @Override
    public String subCmd() {
        return "getprofile";
    }

    @Override
    public String help() {
        return "Looks up a user and it's data when provided a link (preferred) or name. \n**Usage: " + core.PREFIX + " " + subCmd() + "**\n If you cannot find a user when using a name, provide a link to the profile instead.";
    }

    @Override
    public String usage() {
        return subCmd() + " <steam link/custom url name>";
    }

    @Override
    public String description() {
        return "Finds user details.";
    }

    @Override
    public int cooldown() {
        return 20;
    }

    @Override
    public boolean isAdminCmd() {
        return false;
    }

    private HashMap<Long, Long> cooldownMap = new HashMap<>();

    @Override
    public HashMap<Long, Long> cooldownMap() {
        return cooldownMap;
    }

    int emptyCount = 0;
    private String checkNull(Callable<String> runnable) {
        try {
            return runnable.call();
        } catch (NullPointerException e) {
            emptyCount++;
            return "Private";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
