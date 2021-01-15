package io.github.eylexlive.discordpapistats;

import io.github.eylexlive.discordpapistats.command.DiscordStatsCommand;
import io.github.eylexlive.discordpapistats.command.DiscordStatsTabCompleter;
import io.github.eylexlive.discordpapistats.command.discord.StatsCommand;
import io.github.eylexlive.discordpapistats.stats.StatsDatabase;
import io.github.eylexlive.discordpapistats.stats.StatsManager;
import io.github.eylexlive.discordpapistats.util.Metrics;
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

    private StatsDatabase statsDatabase;

    private StatsManager statsManager;

    private JDA jda = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
                CompletableFuture.runAsync(() -> statsManager.saveStats(event.getPlayer()));
            }

        }, this);

        CompletableFuture.runAsync(() -> {
            if (jda != null)
                jda.shutdown();

            try {
                jda = new JDABuilder(AccountType.BOT)
                        .setToken(getConfig().getString("bot-token"))
                        .setAutoReconnect(true)
                        .addEventListeners(new StatsCommand(this))
                        .build();
            } catch (LoginException e) {
                e.printStackTrace();
            }
        });

        new Metrics(this);

        getServer().getScheduler().runTaskTimerAsynchronously(
                this, () -> statsManager.saveAll(),100L, 1200L
        );
    }
    @EventHandler
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
    public StatsDatabase getStatsDatabase() {
        return statsDatabase;
    }

    @NotNull
    public StatsManager getStatsManager() {
        return statsManager;
    }
}
