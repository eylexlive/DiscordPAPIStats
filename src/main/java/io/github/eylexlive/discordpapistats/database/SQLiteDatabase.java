package io.github.eylexlive.discordpapistats.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class SQLiteDatabase extends StatsDatabase {

    private Connection connection;

    @Override
    public StatsDatabase connect() {
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
        return this;
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
    public boolean update(String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String get(String sql, String columnLabel) {
        try (ResultSet result = connection.prepareStatement(sql).executeQuery()) {
            if (result.next()) {
                return result.getString(columnLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    @Override
    public Connection getConnection() {
        return connection;
    }
}
