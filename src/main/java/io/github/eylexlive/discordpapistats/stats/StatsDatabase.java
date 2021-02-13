package io.github.eylexlive.discordpapistats.stats;

import io.github.eylexlive.discordpapistats.DiscordPAPIStats;

import java.io.File;
import java.sql.*;

public final class StatsDatabase {

    private final DiscordPAPIStats plugin;

    private Connection connection;

    public StatsDatabase(DiscordPAPIStats plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "SQLite JDBC Driver not found!"
            );
        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db"
            );

        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean update(String sql) {
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean validateData(String sql) {
        try (ResultSet rs = query(sql)) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet query(String sql) throws SQLException {
        return connection.prepareStatement(sql).executeQuery();
    }
}
