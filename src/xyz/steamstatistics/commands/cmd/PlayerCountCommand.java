package xyz.steamstatistics.commands.cmd;

import me.ugurcan.steamapi.Game;
import me.ugurcan.steamapi.SteamAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;
import xyz.steamstatistics.misc.Logger;
import xyz.steamstatistics.misc.MathUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerCountCommand extends Command {

    private Core core;
    public PlayerCountCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        if (args == null || args[0].equals("") || args[0].equals(" ")) {
            channel.sendMessage(event.getAuthor().getAsMention() + ", you did not provide a valid game name/link.").queue();
        } else {
            /*
            COLLECTING DATA
             */

            Game g = core.steamAPI.getGameFromArgs(args, channel);

            if (g == null) {
                channel.sendMessage("We could not collect data on this game. Try again later.").queue();
                return false;
            }

            String gameId = g.getId();

            Document doc = getDoc(gameId);

            if (doc == null) {
                channel.sendMessage("Not enough data for this game.").queue();
                return false;
            }

            List<CountObj> dataList = new ArrayList<>();

            collectData(dataList, doc, channel);
            Collections.reverse(dataList);

            if (dataList.size() < 5) {
                channel.sendMessage("Not enough data for this game.").queue();
                return false;
            }

            /*
            COMPILING DATA
             */

            String[] counts;
            try {
                counts = doc.getElementsByClass("app-stat").text().trim().split(" ");
            } catch (NullPointerException e) {
                channel.sendMessage("Could not collect data on this game.").queue();
                return false;
            }
            String currentCount = NumberFormat.getInstance().format(Integer.parseInt(counts[0]));
            String dayPeak = NumberFormat.getInstance().format(Integer.parseInt(counts[2]));
            String allTimePeak = NumberFormat.getInstance().format(Integer.parseInt(counts[5]));


            /*
            DRAWING DATA
             */

            File file = new File(this.getClass().getResource("/xyz/steamstatistics/images/playerCountTemplate.png").getPath());
            BufferedImage img = null;

            try {
                img = ImageIO.read(file);
            } catch (IOException ignored) {}

            Graphics2D g2 = (Graphics2D) img.getGraphics();
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHints(rh);

            double barWidth = 652.0;
            double graphHeight = 264.0;
            int blockWidth = (int) Math.round(barWidth / dataList.size());

            final int startX = 90, y = 321;
            int x = startX;
            double upperBound = getUpper(allTimePeak);

            Color endColor = new Color(64, 90, 230);
            Color startColor = new Color(214, 34, 55);
            GradientPaint gradientPaint = new GradientPaint(0, 0, startColor, 0,
                    Math.round((Double.parseDouble(allTimePeak.replaceAll(",", "")) / upperBound * 1.5) * graphHeight), endColor);
            g2.setPaint(gradientPaint);

            for (CountObj data : dataList) {
                double n = Double.parseDouble(data.getCount().replaceAll(",", ""));
                double hPercentage = n / upperBound;
                int h = (int) Math.round(graphHeight * hPercentage);
                g2.fillRect(x, y - h, blockWidth, h);
                x = x + blockWidth;
            }

            // Numbers
            g2.setColor(new Color(250, 127, 113));
            g2.setFont(new Font("Whitney-Bold", Font.PLAIN, 20));

            g2.drawString(MathUtil.format(upperBound), 55 - ((MathUtil.format(upperBound).length() - 1) * 10), 65); // top bar
            g2.drawString(MathUtil.format(upperBound * (2.0 / 3.0)), 55 - ((MathUtil.format(upperBound * (2.0 / 3.0)).length() - 1) * 10), 153); // top middle
            g2.drawString(MathUtil.format(upperBound * (1.0 / 3.0)), 55 - ((MathUtil.format(upperBound * (1.0 / 3.0)).length() - 1) * 10), 241); // bottom middle
            g2.drawString("0", 55, 328); // bottom bar

            // Dates
            g2.setColor(new Color(60, 214, 190));
            g2.setFont(new Font("Whitney-Book", Font.PLAIN, 20));
            int totalTerms = dataList.size();
            List<String> entries = new ArrayList<>();
            entries.add(dataList.get(0).month);
            entries.add(dataList.get(totalTerms / 4).month);
            entries.add(dataList.get(3*(totalTerms) / 4).month);
            entries.add(dataList.get(totalTerms - 2).month);

            int dateY = 348;
            int dateX = 80;
            for (String date : entries) {
                char[] charMonth = date.split(" ")[0].toCharArray();
                String month = "" + charMonth[0] + charMonth[1] + charMonth[2];
                String year = date.split(" ")[1];
                g2.drawString(month + " " + year, dateX, dateY);
                dateX = (int) Math.round(dateX + (barWidth / 3.4));
            }

            /*
            SEND
             */

            File output = null;
            String url = null;
            String extention = ".png";
            long current = System.currentTimeMillis();
            try {
                url = "http://www.jaffaaaa.xyz/steamstats/" + current + extention;
                output = File.createTempFile(current + "", extention);
                ImageIO.write(img, "png", output);
            } catch (IOException ignored) {}

            boolean complete = core.ftp.upload(output, current + extention);

            if (complete) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setAuthor("[Player Count] " + g.getTitle(), "https://store.steampowered.com/app/" + g.getId(), g.getThumbnailURL())
                        .setColor(Color.GREEN)
                        .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png")
                        .setDescription("Player count for the following game: " + g.getTitle() + "\n[Note: Only has data after July 2012]")
                        .setImage(url)
                        .addField(":chart_with_upwards_trend:  Current Count:", currentCount, true)
                        .addField(":chart_with_upwards_trend:  24-hour Peak:", dayPeak, true)
                        .addField(":chart_with_upwards_trend:  All Time Peak:", allTimePeak, true);

                channel.sendMessage(eb.build()).queue();
                output.delete();
                return true;
            } else {
                channel.sendMessage("We could not complete your request at this time. Sorry.").queue();
            }
        }
        return false;
    }

    /*
    Please don't look at this. It ashames me. I am sorry. Please.
    Please
    fucking please
     */
    private double getUpper(String allTimePeak) {
        int i = Integer.parseInt(allTimePeak.replaceAll(",", ""));
        int charAmount = allTimePeak.toCharArray().length;

        if (i < 10) {
            return 10;
        } else if (i < 50) {
            return 50;
        } else if (i < 100) {
            return 100;
        } else if (i < 500) {
            return 500;
        } else if (i < 1000) {
            return 1000;
        } else if (i < 2000) {
            return 2000;
        } else if (i < 5000) {
            return 5000;
        } else if (i < 10000) {
            return 10000;
        } else if (i < 20000) {
            return 20000;
        } else if (i < 30000) {
            return 30000;
        } else if (i < 40000) {
            return 40000;
        } else if (i < 50000) {
            return 50000;
        } else if (i < 75000) {
            return 75000;
        } else if (i < 100000) {
            return 100000;
        } else if (i < 150000) {
            return 150000;
        } else if (i < 200000) {
            return 200000;
        } else if (i < 300000) {
            return 300000;
        } else if (i < 500000) {
            return 500000;
        } else if (i < 750000) {
            return 750000;
        } else if (i < 1000000) {
            return 1000000;
        } else if (i < 1250000) {
            return 1250000;
        } else if (i < 1500000) {
            return 1500000;
        } else if (i < 1750000) {
            return 1750000;
        } else if (i < 2000000) {
            return 2000000;
        } else if (i < 2500000) {
            return 2500000;
        } else if (i < 3000000) {
            return 3000000;
        } else {
            return 5000000;
        }
    }

    private Document getDoc(String gameId) {
        try {
            return Jsoup.connect("http://steamcharts.com/app/" + gameId + "#All").timeout(SteamAPI.timeout).get();
        } catch (HttpStatusException e) {
            return null;
        } catch (IOException e) {
            Logger.error("IOException when connecting to http://steamcharts.com/app/" + gameId + ". Error: ", this.getClass());
            e.printStackTrace();
            return null;
        }
    }

    private void collectData(List<CountObj> dataList, Document doc, TextChannel channel) {
        Elements data;
        try {
            data = doc.getElementsByClass("common-table").first().getAllElements();
        } catch (NullPointerException e) {
            return;
        }

        int check = 0;
        for (Element e : data) {
            String month = "";
            String peakCount = "0";
            try {
                month = e.getElementsByClass("month-cell left").first().text().trim();
                peakCount = e.getElementsByClass("right num").first().text();
            } catch (NullPointerException ex) {
                try {
                    month = e.getElementsByClass("month-cell left italic").first().text().trim();
                    peakCount = e.getElementsByClass("right num italic").first().text();
                } catch (NullPointerException ignored) {}
            }

            if (Integer.parseInt(peakCount) != 0) {
                check++;
                if (check > 2) {
                    CountObj obj = new CountObj(month, peakCount);
                    dataList.add(obj);
                }
            }
        }
    }

    @Override
    public String icon() {
        return ":chart_with_upwards_trend:";
    }
    @Override
    public String subCmd() {
        return "playercount";
    }
    @Override
    public String help() {
        return "This command is used to collect recent playercounts of a game.\n**Usage: *" + core.PREFIX + " " + usage() + "***\nIf the game you're looking for is small you" +
                " may need to provide a link to the game instead of the name.";
    }
    @Override
    public String usage() {
        return "playercount <game name/link>";
    }
    @Override
    public String description() {
        return "Display's playercount data about a game.";
    }

    @Override
    public boolean isAdminCmd() {
        return false;
    }

    @Override
    public int cooldown() {
        return 30;
    }
    private HashMap<Long, Long> cooldownMap = new HashMap<>();
    @Override
    public HashMap<Long, Long> cooldownMap() {
        return cooldownMap;
    }
    public class CountObj {

        private String month;
        private String count;

        public CountObj(String month, String count) {
            this.month=month;
            this.count=count;
        }

        public String getMonth() {
            return this.month;
        }

        public String getCount() {
            return this.count;
        }

    }
}
