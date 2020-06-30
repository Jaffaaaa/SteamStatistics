package xyz.steamstatistics.commands.cmd;

import me.ugurcan.steamapi.Game;
import me.ugurcan.steamapi.SteamAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.steamstatistics.Core;
import xyz.steamstatistics.commands.Command;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class NewReleasesCommand extends Command {

    private Core core;
    public NewReleasesCommand(Core core) {
        this.core=core;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String raw, TextChannel channel, String[] args) {
        String opt;
        if (args == null || args[0].equals("") || args[0].equals(" ")) {
            opt = "FEATURED";
        } else {
            String arg = args[0].toUpperCase();
            if (!arg.equals("FEATURED") && !arg.equals("ALL")) {
                channel.sendMessage("You didn't provide a valid type. Pick 'featured' or 'all'.\n*Usage: " + core.PREFIX + " " + usage() + "*").queue();
                return false;
            }
            opt = arg;
        }

        Document doc;
        try {
            doc = Jsoup.connect("https://store.steampowered.com/explore/new/").timeout(SteamAPI.timeout).get();
            channel.sendMessage("Collecting data...").queue();
        } catch (IOException e) {
            channel.sendMessage("Could not connect to Steam, try again later.").queue();
            return false;
        }

        Elements elements;
        if (opt.equals("ALL")) {
            elements = doc.getElementsByClass("tab_content").get(3).getElementsByClass("tab_item_content");
        } else {
            elements = doc.getElementsByClass("tab_content").get(2).getElementsByClass("tab_item_content");
        }

        // Duplicate check
        for (int i = 0; i < elements.size(); i++) {
            for (int j = i + 1 ; j < elements.size(); j++) {
                if (elements.get(i).text().equalsIgnoreCase(elements.get(j).text())) {
                    elements.remove(i);
                }
            }
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor("[New Releases] " + WordUtils.capitalizeFully(opt), "https://store.steampowered.com/explore/new/", "https://i.gyazo.com/44030cf35126bc2fe77f49b068fcef6a.png")
                .setDescription("Showing top five latest released games, searched by **" + opt.toLowerCase() + "**.")
                .setColor(Color.cyan)
                .setFooter("Requested by " + event.getAuthor().getAsTag() + ".", "https://i.gyazo.com/bff4d240bc09b96eac07544cc1a9d4b6.png");

        for (int i = 0; i < 5; i++) {
            try {
                Element e = elements.get(i);
                String name = e.getElementsByClass("tab_item_name").get(0).text().trim();
                Game g = core.steamAPI.getClosestGame(name);
                core.steamAPI.fillWithDetails(g);
                String price = g.getPrice();
                String desc = g.getDescription();
                String url = "https://store.steampowered.com/app/" + g.getId() + "/";
                eb.addField("", "**" + name + " [" + price + "]**\n" + desc + "\n[" + url + "]", false);
            } catch (NullPointerException ignored) {
                elements.remove(i);
                i = i - 1;
            }
        }

        channel.sendMessage(eb.build()).queue();
        return true;
    }

    @Override
    public String icon() {
        return ":tickets:";
    }

    @Override
    public String subCmd() {
        return "newreleases";
    }

    @Override
    public String help() {
        return "This command is used to collect new releases on Steam.\n**Usage: *" + core.PREFIX + " " + usage();
    }

    @Override
    public String usage() {
        return subCmd() + " <featured/all>";
    }

    @Override
    public String description() {
        return "Gets new game releases.";
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
}
