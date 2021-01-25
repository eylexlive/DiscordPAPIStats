package io.github.eylexlive.discordpapistats.stats;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class StatsManager {

    private final DiscordPAPIStats plugin;

    private final List<Stats> statsList = new ArrayList<>();

    public StatsManager(DiscordPAPIStats plugin) {
        this.plugin = plugin;
    }

    public void loadStats() {
        plugin.getLogger().info(
                "[l] Loading all stats... "
        );

        try (ResultSet result = plugin.getStatsDatabase().query("select name from sqlite_master where type ='table' and name not like 'sqlite_%'")) {
            while (result.next()) {
                final String[] parts = result.getString(1).split("%");
                if (parts.length != 2)
                    continue;

                statsList.add(
                        new Stats(parts[0], parts[1])
                );

                plugin.getLogger().info(
                        "[l] Loaded " + parts[0]
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        plugin.getLogger().info(
                "[l] Successfully loaded " + statsList.size() + " stat" + (statsList.size() > 1 ? "s." : ".")
        );
    }

    public boolean createStats(Stats stats) {
        final boolean success = plugin.getStatsDatabase().update(
                "create table if not exists '" + stats.getTableName() + "' (name TEXT PRIMARY KEY, value TEXT)"
        );

        if (success) {
            statsList.add(stats);
            CompletableFuture.runAsync(
                    this::saveAll
            );
        }

        return success;
    }

    public boolean deleteStats(Stats stats) {
        final boolean success = plugin.getStatsDatabase().update(
                "drop table '" + stats.getTableName() + "'"
        );

        if (success) statsList.remove(stats);

        return success;
    }

    public boolean setName(Stats stats, String newName) {
        final boolean success = plugin.getStatsDatabase().update(
               "alter table '" + stats.getTableName() + "' rename to '" + newName + "%" + stats.getPlaceholder() + "'"
        );

        if (success) stats.setName(newName);

        return success;
    }

    public boolean setPlaceholder(Stats stats, String newPlaceholder) {
        final boolean success = plugin.getStatsDatabase().update(
                "alter table '" + stats.getTableName() + "' rename to '" + stats.getName() + "%" + newPlaceholder + "'"
        );

        if (success) stats.setPlaceholder(newPlaceholder);

        return success;
    }


    public String getStats(Stats stats, String name) {
        try (ResultSet result = plugin.getStatsDatabase().query("select * from '" + stats.getTableName() + "' where lower(name) = lower('" + name.toLowerCase() + "')")) {
            if (result.next())
                return result.getString("value");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ConfigUtil.getString(
                "no-data-available"
        );
    }

    public String getStats(Stats stats, Player player) {
        return PlaceholderAPI.setPlaceholders(
                player,
                "%" + stats.getPlaceholder() + "%"
        );
    }

    public void saveStats(Player player) {
        statsList.forEach(stats -> {
            final String value = PlaceholderAPI.setPlaceholders(player, "%" + stats.getPlaceholder() + "%");
            if (!plugin.getStatsDatabase().validateData("select * from '" + stats.getTableName() + "' where name = '"+ player.getName() + "'")) {
                plugin.getStatsDatabase().update(
                        "insert into '" + stats.getTableName() + "' (name, value) values ('" + player.getName()+"', '" + value + "')"
                );
            } else {
                plugin.getStatsDatabase().update(
                        "update '" + stats.getTableName() + "' set value = '" + value + "' where name='" + player.getName() + "'"
                );
            }

        });
    }

    public void saveAll() {
        plugin.getServer()
                .getOnlinePlayers()
                .forEach(
                        this::saveStats
                );
    }

    public Stats getStatsByName(String name) {
        for (Stats stats : statsList) {
            if (stats.getName().equalsIgnoreCase(name))
                return stats;
        }
        return null;
    }

    public List<Stats> getStatsList() {
        return statsList;
    }
}
