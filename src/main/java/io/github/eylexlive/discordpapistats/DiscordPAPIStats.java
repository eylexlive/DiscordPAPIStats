package io.github.eylexlive.discordpapistats;

import io.github.eylexlive.discordpapistats.command.DiscordStatsCommand;
import io.github.eylexlive.discordpapistats.command.DiscordStatsTabCompleter;
import io.github.eylexlive.discordpapistats.command.discord.StatsCommand;
import io.github.eylexlive.discordpapistats.stats.StatsDatabase;
import io.github.eylexlive.discordpapistats.stats.StatsManager;
import io.github.eylexlive.discordpapistats.util.Metrics;
import io.github.eylexlive.discordpapistats.util.UpdateCheck;
import io.github.eylexlive.discordpapistats.util.config.Config;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.concurrent.CompletableFuture;

public final class DiscordPAPIStats extends JavaPlugin implements Listener {

    private static DiscordPAPIStats instance;

    private Config config;

    private StatsDatabase statsDatabase;

    private StatsManager statsManager;

    private JDA jda = null;

    @Override
    public void onEnable() {
        if (instance != null)
            throw new IllegalStateException("DiscordPAPIStats can not be started twice!");
        instance = this;

        config = new Config("config");

        statsDatabase = new StatsDatabase(this);
        statsDatabase.init();

        statsManager = new StatsManager(this);
        statsManager.loadStats();

        getCommand("discordstats").setExecutor(
                new DiscordStatsCommand(this)
        );
        getCommand("discordstats").setTabCompleter(
                new DiscordStatsTabCompleter(this)
        );

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.MONITOR)
            public void handleJoinEvent(PlayerJoinEvent event) {
                // Save data to see offline player stats **NOT WORKS ON QUIT EVENT**
                CompletableFuture.runAsync(() -> statsManager.saveStats(event.getPlayer()));
            }

        }, this);

        new UpdateCheck(this);

        CompletableFuture.runAsync(() -> {
            if (jda != null)
                jda.shutdown();

            try {
                jda = new JDABuilder(AccountType.BOT)
                        .setToken(config.getString("bot-token"))
                        .setAutoReconnect(true)
                        .addEventListeners(new StatsCommand(this))
                        .build();
            } catch (LoginException e) {
                e.printStackTrace();
            }
        });

        new Metrics(this);

        // Save data every minute to see offline player stats
        getServer().getScheduler().runTaskTimerAsynchronously(
                this, () -> statsManager.saveAll(), 100L, 1200L
        );
    }

    @Override
    public void onDisable() {
        if (statsDatabase != null)
            statsDatabase.close();

        if (jda != null) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onShutdown(@NotNull ShutdownEvent event) {
                    future.complete(null);
                }
            });
            jda.shutdownNow();
        }
    }

    @NotNull
    public static DiscordPAPIStats getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public Config getConfig() {
        return config;
    }

    @NotNull
    public StatsDatabase getStatsDatabase() {
        return statsDatabase;
    }

    @NotNull
    public StatsManager getStatsManager() {
        return statsManager;
    }
}
