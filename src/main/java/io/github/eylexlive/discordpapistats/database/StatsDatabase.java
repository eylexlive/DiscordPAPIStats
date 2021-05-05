package io.github.eylexlive.discordpapistats.database;

import io.github.eylexlive.discordpapistats.stats.Stats;
import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class StatsDatabase {

    public abstract void connect();

    public abstract void close();

    public abstract boolean createStatsTable(Stats stats);

    public abstract boolean dropStatsTable(Stats stats);

    public abstract boolean updateStatsName(Stats stats, String name);

    public abstract boolean updateStatsPlaceholder(Stats stats, String placeholder);

    public abstract String getPlayerStats(Stats stats, String name);

    public abstract void updatePlayerStats(Stats stats, Player player);

    public abstract List<String> getTables();

    public List<String> getStats() {
        final List<String> tables = getTables();
        tables.removeIf(table ->
                !table.contains(
                        ConfigUtil.getStringSafely(
                                "stats-separator"
                        )
                )
        );
        return tables;
    }
}
