package xyz.steamstatistics;

import me.ugurcan.steamapi.CountryCode;
import me.ugurcan.steamapi.Language;
import me.ugurcan.steamapi.SteamAPI;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import xyz.steamstatistics.commands.CommandManager;
import xyz.steamstatistics.commands.cmd.*;
import xyz.steamstatistics.data.GameShort;
import xyz.steamstatistics.misc.FTPHandling;
import xyz.steamstatistics.misc.SteamFunc;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;

public class Core {

    public String TOKEN;
    public final String PREFIX = ".s";

    public JDA jda;
    public CommandManager commandManager;
    public SteamAPI steamAPI;
    public FTPHandling ftp;
    public String steamKey;

    public void start(String token, String ftpUser, String ftpPass, String steamKey) throws LoginException {
        this.TOKEN = token;

        this.jda = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setActivity(Activity.playing(PREFIX + " help"))
                .build();

        commandManager = new CommandManager(this)
                .add(new HelpCommand(this))
                .add(new PlayerCountCommand(this))
                .add(new GetProfileCommand(this))
                .add(new GetProfilePicCommand(this))
                .add(new GameLookupCommand(this))
                .add(new CSGOStatsCommand(this))
                .add(new TF2StatsCommand(this))
                .add(new RustStatsCommand(this))
                .add(new NewReleasesCommand(this))
                .add(new BotStatsCommand(this))
                .build();

        this.steamAPI = new SteamAPI(CountryCode.US, Language.ENGLISH);
        this.ftp = new FTPHandling(this, ftpUser, ftpPass);
        this.steamKey = steamKey;

        new SteamFunc(this);
        GameShort.apply();
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }

}
