package io.github.eylexlive.discordpapistats;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.eylexlive.discordpapistats.command.DiscordStatsCommand;
import io.github.eylexlive.discordpapistats.command.discord.StatsCommand;
import io.github.eylexlive.discordpapistats.database.MySQLDatabase;
import io.github.eylexlive.discordpapistats.database.SQLiteDatabase;
import io.github.eylexlive.discordpapistats.database.StatsDatabase;
import io.github.eylexlive.discordpapistats.stats.StatsManager;
import io.github.eylexlive.discordpapistats.util.UpdateCheck;
import io.github.eylexlive.discordpapistats.util.config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class DiscordPAPIStats extends JavaPlugin {

    private static DiscordPAPIStats instance;

    private Config config;

    private StatsDatabase statsDatabase;

    private StatsManager statsManager;

    private DiscordSRV discordSRV;

    private JDA jda;

    @Override
    public void onEnable() {
        instance = this;

        config = new Config("config");

        final PluginManager manager = Bukkit.getPluginManager();

        if (manager.isPluginEnabled("PlugMan")) {
            final Plugin plugManPlugin = manager.getPlugin("PlugMan");
            try {
                final List<String> ignoredPlugins = (List<String>) plugManPlugin.getClass().getMethod("getIgnoredPlugins").invoke(plugManPlugin);
                if (!ignoredPlugins.contains("DiscordPAPIStats")) {
                    ignoredPlugins.add("DiscordPAPIStats");
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        if (manager.isPluginEnabled("DiscordSRV")) {
            discordSRV = DiscordSRV.getPlugin();
            getLogger().info(
                    "[l] Hooked into DiscordSRV"
            );
        }

        statsDatabase = isSQL() ? new MySQLDatabase() : new SQLiteDatabase();
        statsDatabase.connect();

        final PluginCommand cmd = getCommand("discordstats");
        if (cmd != null) {
            cmd.setExecutor(new DiscordStatsCommand(this));
            cmd.setTabCompleter(new DiscordStatsCommand(this));
        }

        manager.registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.MONITOR)
            public void handleJoinEvent(PlayerJoinEvent event) {
                // Save data to see offline player stats **NOT WORKS ON QUIT EVENT**
                Bukkit.getScheduler().runTaskAsynchronously(instance, () ->
                        statsManager.saveStats(event.getPlayer())
                );
            }

        }, this);

        new UpdateCheck(this);

        statsManager = new StatsManager(this);
        statsManager.load();

        Bukkit.getScheduler().runTask(this, () -> {
            if (jda != null) {
                jda.shutdown();
            }

            try {
                jda = JDABuilder.createDefault(config.getString("bot-token"))
                        .setAutoReconnect(true)
                        .addEventListeners(new StatsCommand(this))
                        .build();
            } catch (LoginException e) {
                e.printStackTrace();
            }

            if (config.getBoolean("bot-activity.enabled")) {
                jda.getPresence().setActivity(
                        Activity.of(
                                Activity.ActivityType.valueOf(
                                        config.getString("bot-activity.type")
                                ),
                                config.getString("bot-activity.value")
                        )
                );
            }
        });

        // Metrics
        new Metrics(this, 10011);

        // Save data every minute to see offline player stats
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
                Bukkit.getOnlinePlayers().forEach(p ->
                        statsManager.saveStats(p)
                ), 100L, 1200L
        );
    }

    @Override
    public void onDisable() {
        if (statsDatabase != null) {
            statsDatabase.close();
        }

        if (jda != null) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onShutdown(@NotNull ShutdownEvent event) {
                    future.complete(null);
                }
            });
            jda.shutdownNow();
            jda = null;

            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                getLogger().warning(
                        "JDA took too long to shutdown, skipping!"
                );
            }
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

    @Nullable
    public DiscordSRV getDiscordSRV() {
        return discordSRV;
    }

    public boolean isSQL() {
        return config.getBoolean("mysql.enabled");
    }
}
