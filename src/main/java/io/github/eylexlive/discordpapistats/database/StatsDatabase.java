package io.github.eylexlive.discordpapistats.database;

import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;

import java.util.List;

public abstract class StatsDatabase {

    public abstract void connect();

    public abstract void close();

    public abstract boolean update(String sql);

    public abstract String get(String sql, String columnLabel);

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
