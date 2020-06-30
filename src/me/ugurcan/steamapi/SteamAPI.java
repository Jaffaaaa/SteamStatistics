package me.ugurcan.steamapi;

import net.dv8tion.jda.api.entities.TextChannel;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.steamstatistics.data.GameShort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SteamAPI {

    /*
    UGURCAN'S CODE
     */

    private String cc;
    private String lang;

    public static final int timeout = 10000;

    public SteamAPI(CountryCode countryCode, Language language) {
        this.cc = countryCode.toString().toLowerCase(Locale.ENGLISH);
        this.lang = language.toString().toLowerCase(Locale.ENGLISH);
    }

    public Games searchForGames(String gameTitle, int numOfResults, SearchMode searchMode) {
        gameTitle = gameTitle.toLowerCase(Locale.ENGLISH);
        String sortBy = searchMode.getSortBy();

        Games games = new Games();

        if (gameTitle.length() < 2) {
            System.out.println("Invalid param: gameTitle");
            return games;
        }
        if (numOfResults <= 0) {
            System.out.println("Invalid param: numOfResults");
            return games;
        }

        try {
            int count = 0;
            int page = 0;
            boolean stillFound = true;

            while (stillFound) {
                stillFound = false;
                page++;

                Document doc;
                try {
                    doc = Jsoup.connect("http://store.steampowered.com/search/?term=" + gameTitle + "&sort_by=" + sortBy + "&page=" + page + "&cc=" + cc + "&l=" + lang + "&category1=998%2C994")
                            .timeout(timeout)
                            .get();
                } catch (HttpStatusException e) {
                    return null;
                }

                Elements elements = doc.getElementsByAttributeValue("id", "search_result_container").select("a");

                for (Element element : elements) {
                    String id = element.attr("data-ds-appid").trim();
                    if (id.equals(""))
                        continue;

                    String title = element.getElementsByClass("title").text().trim();
                    String discountPercent = element.getElementsByClass("search_discount").text().trim();
                    String price;
                    String discountedPrice;
                    if (discountPercent.equals("")) {
                        price = element.getElementsByClass("search_price").text().trim();
                        discountedPrice = "";
                    } else {
                        Elements priceElm = element.getElementsByClass("search_price");

                        int startIndex = priceElm.toString().indexOf("<br>") + 4;
                        int endIndex = priceElm.toString().indexOf("</div>");

                        price = priceElm.select("strike").text();
                        discountedPrice = priceElm.toString().substring(startIndex, endIndex).trim();
                    }

                    ArrayList<String> platforms = new ArrayList<String>();
                    Elements platformElms = element.select("p").select("span");
                    for (Element platformElm : platformElms) {
                        String platform = platformElm.attr("class").split(" ")[0].trim();
                        platforms.add(platform);
                    }

                    String reviewSummary = element.getElementsByClass("search_review_summary").attr("data-store-tooltip").trim();
                    if (!reviewSummary.equals("")) {
                        String[] reviewSummaryArray = reviewSummary.split("<br>");
                        reviewSummary = reviewSummaryArray[0] + " (" + reviewSummaryArray[1] + ")";
                    }

                    String addedOn = element.getElementsByClass("search_released").text().trim();

                    String thumbnailURL = element.select("img").attr("src").trim();

                    games.add(new Game(id, title, price, discountPercent, discountedPrice, reviewSummary, platforms, addedOn, thumbnailURL));

                    stillFound = true;
                    count++;
                    if (count == numOfResults)
                        break;
                }

                if (count == numOfResults)
                    break;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            games.clear();
        }

        return games;
    }

    public void fillWithDetails(Game game) {
        if (game == null || game.getId().equals("")) {
            return;
        }

        try {

            /*Connection.Response agecheckForm = Jsoup.connect("http://store.steampowered.com/agecheck/app/" + game.getId()).timeout(10000)
                    .data("snr", "1_agecheck_agecheck__age-gate")
                    .data("ageDay", "1")
                    .data("ageMonth", "January")
                    .data("ageYear", "1900")
                    .method(Connection.Method.POST)
                    .execute();

            System.out.println(agecheckForm.cookies());*/

            Document doc = Jsoup.connect("http://store.steampowered.com/app/" + game.getId() + "?l=" + lang + "&cc=" + cc).timeout(timeout)
                    /*.data("snr", "1_agecheck_agecheck__age-gate")
                    .data("ageDay", "1")
                    .data("ageMonth", "January")
                    .data("ageYear", "1900")
                    .cookies(agecheckForm.cookies())*/
                    .cookie("birthtime", "-2208959999")
                    .get();

            //description
            String description = doc.getElementsByClass("game_description_snippet").text().trim();
            game.setDescription(description);

            //headerImageURL
            String headerImageURL = doc.getElementsByAttributeValue("rel", "image_src").attr("href").trim();
            game.setHeaderImageURL(headerImageURL);

            //screenshotURLs
            ArrayList<String> screenshotURLs = new ArrayList<String>();
            Elements ssUrlElms = doc.getElementsByClass("highlight_screenshot_link");
            for (Element ssUrlElm : ssUrlElms) {
                String screenshotURL = ssUrlElm.attr("href").trim();
                screenshotURLs.add(screenshotURL);
            }
            game.setScreenshotURLs(screenshotURLs);

            //release date
            String releaseDate = doc.getElementsByClass("date").text().trim();
            game.setReleaseDate(releaseDate);

            //metascore
            String metascore = doc.getElementsByAttributeValue("id", "game_area_metascore").text().trim();
            game.setMetascore(metascore);

            //details
            ArrayList<String> details = new ArrayList<String>();
            Elements detailElms = doc.getElementsByClass("game_area_details_specs");
            for (Element detailElm : detailElms) {
                String detail = detailElm.text().trim();
                details.add(detail);
            }
            game.setDetails(details);

            //tags
            ArrayList<String> tags = new ArrayList<String>();
            Elements tagElms = doc.getElementsByClass("glance_tags").select("a");
            for (Element tagElm : tagElms) {
                String tag = tagElm.text().trim();
                tags.add(tag);
            }
            game.setTags(tags);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
    MY CODE
     */

    public Game getClosestGame(String title) {
        title = title.substring(0, title.length() - 1);
        if (GameShort.contains(title)) {
            title = GameShort.getTitle(title);
        }

        Games games = searchForGames(title, 10, SearchMode.RELEVANCE);

        SimilarityStrategy strategy = new JaroWinklerStrategy();
        StringSimilarityService service = new StringSimilarityServiceImpl(strategy);

        if (games.size() == 0) {
            return null;
        }

        Game finalGame = games.get(0);
        double score = 0.0;
        for (Game game : games) {
            String target = game.getTitle();
            double newScore = service.score(title, target);
            if (newScore > score) {
                score = newScore;
                finalGame = game;
            }
        }

        return finalGame;
    }

    public Game getGameFromArgs(String[] args, TextChannel channel) {
        Game g = null;
        if (args[0].contains("http")) {
            String link = args[0];
            try {
                g = getGameFromLink(link);
            } catch (IOException e) {
                if (channel != null) {
                    channel.sendMessage("Connection timed out... Try again later.").queue();
                }
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            String title = sb.toString();

            if (channel != null) {
                channel.sendMessage("Loading data....").queue();
            }

            try {
                g = getClosestGame(title);
            } catch (IndexOutOfBoundsException e) {
                if (channel != null) {
                    channel.sendMessage("You did not provide a valid game name/link.").queue();
                }
                return null;
            }
        }

        if (g != null) {
            fillWithDetails(g);
        }

        return g;
    }

    public Game getGameFromLink(String l) throws IOException {
        Document doc = Jsoup.connect(l).timeout(timeout).get();
        String title = doc.getElementsByClass("apphub_AppName").text().trim();
        return getClosestGame(title);
    }
}