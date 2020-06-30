package xyz.steamstatistics.events;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.steamstatistics.Core;

import javax.annotation.Nonnull;

public class OnGuildJoinEvent extends ListenerAdapter {

    private Core core;
    public OnGuildJoinEvent(Core core) {
        this.core=core;
        core.jda.addEventListener(this);
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        core.reportServerCount();
    }
}
