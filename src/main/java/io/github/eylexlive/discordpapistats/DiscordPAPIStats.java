package io.github.eylexlive.discordpapistats;

import github.scarsz.discordsrv.DiscordSRV;
import io.github.eylexlive.discordpapistats.command.DiscordStatsCommand;
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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.concurrent.CompletableFuture;

public final class DiscordPAPIStats extends JavaPlugin {

    private static DiscordPAPIStats instance;

    private Config config;

    private StatsDatabase statsDatabase;

    private StatsManager statsManager;

    private DiscordSRV discordSRV;

    private JDA jda = null;

    @Override
    public void onEnable() {
        if (instance != null)
            throw new IllegalStateException(
                    "DiscordPAPIStats can not be started twice!"
            );

        instance = this;

        config = new Config("config");

        if (getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRV = DiscordSRV.getPlugin();
            getLogger().info(
                    "[l] Hooked into DiscordSRV"
            );
        }

        statsDatabase = new StatsDatabase(this);

        if (!statsDatabase.init()) {
            getLogger().warning("[e] Error while loading the database!");
        }

        statsManager = new StatsManager(this);

        if (!statsManager.load()) {
            getLogger().warning("[e] Error while loading stats!");
        }

        registerCommand(
                "discordstats", new DiscordStatsCommand(this)
        );

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler (priority = EventPriority.MONITOR)
            public void handleJoinEvent(PlayerJoinEvent event) {
                // Save data to see offline player stats **NOT WORKS ON QUIT EVENT**
                CompletableFuture.runAsync(() ->
                        statsManager.saveStats(
                                event.getPlayer()
                        )
                );
            }

        }, this);

        new UpdateCheck(this);

        getServer().getScheduler().runTask(this, () -> {
            if (jda != null)
                jda.shutdown();

            try {
                jda = new JDABuilder(AccountType.BOT)
                        .setAutoReconnect(true)
                        .setToken(config.getString("bot-token"))
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

    private void registerCommand(String cmd, CommandExecutor executor) {
        final PluginCommand command = getCommand(cmd);
        if (command == null) {
            getLogger().warning(
                    "Command cannot be registered: '/" + cmd + "'"
            );
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
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

    public DiscordSRV getDiscordSRV() {
        return discordSRV;
    }
}
