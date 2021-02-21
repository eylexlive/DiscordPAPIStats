package io.github.eylexlive.discordpapistats.stats;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;
import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class StatsManager {

    private final DiscordPAPIStats plugin;

    private final List<Stats> statsList = new ArrayList<>();

    public StatsManager(DiscordPAPIStats plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getLogger().info(
                "[l] Loading all stats... "
        );
        plugin.getStatsDatabase()
                .getStats()
                .forEach(stat -> {
                            final String[] parts = stat.split(
                                    ConfigUtil.getStringSafely(
                                            "stats-separator"
                                    )
                            );

                            if (parts.length == 2) {
                                final Stats stats = new Stats(
                                        parts[0],
                                        parts[1],
                                        ConfigUtil.getStringList(
                                                "stats-filter-list"
                                        ).contains(parts[0])
                                );

                                statsList.add(stats);

                                plugin.getLogger().info(
                                        "[l] Loaded " + stats.getName()
                                );
                            }
                        }
                );
        plugin.getLogger().info(
                "[l] Successfully loaded " + statsList.size() + " stat" + (statsList.size() > 1 ? "s." : ".")
        );
    }

    public boolean createStats(Stats stats) {
        final boolean success = plugin.getStatsDatabase().update(
                "create table if not exists `" + stats.getTableName() + "` (name VARCHAR(255) NOT NULL PRIMARY KEY, value VARCHAR(255) NOT NULL)"
        );

        if (success) {
            statsList.add(stats);
            CompletableFuture.runAsync(() ->
                    Bukkit.getOnlinePlayers().forEach(this::saveStats)
            );
        }

        return success;
    }

    public boolean deleteStats(Stats stats) {
        final boolean success = plugin.getStatsDatabase().update(
                "drop table `" + stats.getTableName() + "`"
        );

        if (success) statsList.remove(stats);

        return success;
    }

    public boolean setName(Stats stats, String newName) {
        final boolean success = plugin.getStatsDatabase().update(
               "alter table `" + stats.getTableName() + "` rename to `" + newName + ConfigUtil.getStringSafely("stats-separator") + stats.getPlaceholder() + "`"
        );

        if (success) stats.setName(newName);

        return success;
    }

    public boolean setPlaceholder(Stats stats, String newPlaceholder) {
        final boolean success = plugin.getStatsDatabase().update(
                "alter table `" + stats.getTableName() + "` rename to `" + stats.getName() + ConfigUtil.getStringSafely("stats-separator") + newPlaceholder + "`"
        );

        if (success) stats.setPlaceholder(newPlaceholder);

        return success;
    }

    public boolean filterStats(Stats stats) {
        final boolean filtered = !stats.isFiltered();

        stats.setFiltered(filtered);

        final List<String> filterList = ConfigUtil.getStringList("stats-filter-list");

        if (filtered)
            filterList.add(stats.getName());
        else
            filterList.remove(stats.getName());

        ConfigUtil.set(
                "stats-filter-list", filterList
        );

        return filtered;
    }

    public String getStats(Stats stats, String name) {
        final String value =  plugin.getStatsDatabase().get(
                "select * from `" + stats.getTableName() + "` where lower(name) = '" + name.toLowerCase() + "'",
                "value"
        );

        return value != null ? value : ConfigUtil.getString("no-data-available");
    }

    public String getStats(Stats stats, Player player) {
        final String placeholder = stats.getPlaceholder();
        final String value = PlaceholderAPI.setPlaceholders(
                player,
                "%" + placeholder + "%"
        );

        if ((placeholder.contains("%") || value.equals("%" + placeholder + "%"))) {
            return getStats(stats, player.getName());
        }

        return value;
    }

    public void saveStats(Player player) {
        statsList.stream()
                .filter(stats -> !stats.isFiltered())
                .forEach(stats -> {
                            final String placeholder = stats.getPlaceholder();
                            final String value = PlaceholderAPI.setPlaceholders(
                                    player,
                                    "%" + placeholder + "%"
                            );

                            if (!(placeholder.contains("%") || value.equals("%" + placeholder + "%"))) {
                                if (plugin.getStatsDatabase().get("select * from `" + stats.getTableName() + "` where name = '"+ player.getName() + "'", "value") == null) {
                                    plugin.getStatsDatabase().update(
                                            "insert into `" + stats.getTableName() + "` (name, value) values ('" + player.getName()+"', '" + value + "')"
                                    );

                                } else {
                                    plugin.getStatsDatabase().update(
                                            "update `" + stats.getTableName() + "` set value = '" + value + "' where name='" + player.getName() + "'"
                                    );
                                }
                            }
                        }
                );
    }

    public Stats getStatsByName(String name, boolean equals) {
        for (Stats stats : statsList) {
            if (equals && stats.getName().equalsIgnoreCase(name)
                    || name.toLowerCase().contains(stats.getName().toLowerCase()))
                return stats;
        }
        return null;
    }

    public List<String> getPerStatsCommands() {
        final List<String> list = new ArrayList<>();
        getStatsNames().forEach(name ->
                list.add(
                        ConfigUtil.getString(
                                "per-stats-commands.command-format",
                                "stats_name:" + name
                        )
                )
        );
        return list;
    }

    public List<String> getStatsNames() {
        return statsList.stream()
                .map(Stats::getName)
                .collect(Collectors.toList());
    }

    public List<Stats> getStatsList() {
        return statsList;
    }
}
