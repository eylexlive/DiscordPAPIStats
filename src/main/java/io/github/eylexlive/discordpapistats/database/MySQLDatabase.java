package io.github.eylexlive.discordpapistats.database;

import com.zaxxer.hikari.HikariDataSource;
import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class MySQLDatabase extends StatsDatabase {

    private HikariDataSource dataSource;

    @Override
    public void connect() {
        dataSource = new HikariDataSource();
        dataSource.setPoolName("DiscordPAPIStatsMYSQLPool");

        dataSource.setJdbcUrl("jdbc:mysql://" +
                ConfigUtil.getStringSafely(
                        "mysql.host"
                ) +
                ":" +
                ConfigUtil.getInt(
                        "mysql.port"
                ) +
                "/" +
                ConfigUtil.getStringSafely(
                        "mysql.database"
                )
        );

        dataSource.setUsername(
                ConfigUtil.getStringSafely(
                        "mysql.username"
                )
        );

        dataSource.setPassword(
                ConfigUtil.getStringSafely(
                        "mysql.password"
                )
        );

        dataSource.addDataSourceProperty(
                "autoReconnect", "true"
        );

        dataSource.addDataSourceProperty(
                "autoReconnectForPools", "true"
        );

        dataSource.addDataSourceProperty(
                "characterEncoding", "UTF-8"
        );

        dataSource.addDataSourceProperty(
                "useSSL", String.valueOf(
                        ConfigUtil.getBoolean(
                                "mysql.use-ssl"
                        )
                )
        );
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public boolean update(String sql) {
        try (Connection conn = getConnection()) {
            conn.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String get(String sql, String columnLabel) {
        try (Connection conn = getConnection(); ResultSet result = conn.prepareStatement(sql).executeQuery()) {
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
        try (Connection conn = getConnection(); ResultSet result = conn.getMetaData().getTables(null, null, "%", new String[] {"TABLE"});) {
            while (result.next()) {
                tables.add(result.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            return null;
        }
        return tables;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
