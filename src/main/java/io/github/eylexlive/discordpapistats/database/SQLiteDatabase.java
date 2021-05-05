package io.github.eylexlive.discordpapistats.database;

import io.github.eylexlive.discordpapistats.stats.Stats;
import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class SQLiteDatabase extends StatsDatabase {

    private Connection connection;

    @Override
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Failed to load SQLite JDBC class", e
            );
        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:plugins/DiscordPAPIStats/database.db"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createStatsTable(Stats stats) {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "create table if not exists `"
                            + stats.getTableName()
                            + "` (name VARCHAR(255) NOT NULL PRIMARY KEY, value VARCHAR(255) NOT NULL)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean dropStatsTable(Stats stats) {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "drop table `" + stats.getTableName() + "`"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean updateStatsName(Stats stats, String name) {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "alter table `" + stats.getTableName()
                            + "` rename to `" + name
                            + ConfigUtil.getStringSafely("stats-separator")
                            + stats.getPlaceholder() + "`"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean updateStatsPlaceholder(Stats stats, String placeholder) {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "alter table `" + stats.getTableName()
                            + "` rename to `" + stats.getName()
                            + ConfigUtil.getStringSafely("stats-separator")
                            + placeholder + "`"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String getPlayerStats(Stats stats, String name) {
        final String sql = "select * from `" + stats.getTableName() + "` where lower(name) = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, name.toLowerCase());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updatePlayerStats(Stats stats, Player player) {
        final String placeholder = stats.getPlaceholder();
        final String value = PlaceholderAPI.setPlaceholders(
                player,
                "%" + placeholder + "%"
        );

        if (!(placeholder.contains("%") || value.equals("%" + placeholder + "%"))) {
            final String sql = "select * from `" + stats.getTableName() + "` where name = ?";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setString(1, player.getName());
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement ips = connection.prepareStatement( "insert into `" + stats.getTableName() + "` (name, value) values (?, ?)")) {
                            ips.setString(1, player.getName());
                            ips.setString(2, value);
                            ips.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ups = connection.prepareStatement("update `" + stats.getTableName() + "` set value = ? where name = ?")) {
                            ups.setString(1, value);
                            ups.setString(2, player.getName());
                            ups.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<String> getTables() {
        final List<String> tables = new ArrayList<>();
        try (ResultSet result = connection.getMetaData().getTables(null, null, "%", new String[] {"TABLE"})) {
            while (result.next()) {
                tables.add(result.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            return null;
        }
        return tables;
    }
}
