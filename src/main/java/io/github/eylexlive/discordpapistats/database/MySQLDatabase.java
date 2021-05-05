package io.github.eylexlive.discordpapistats.database;

import com.zaxxer.hikari.HikariDataSource;
import io.github.eylexlive.discordpapistats.stats.Stats;
import io.github.eylexlive.discordpapistats.util.config.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

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

        dataSource.addDataSourceProperty(
                "prepStmtCacheSize", "250"
        );

        dataSource.addDataSourceProperty(
                "prepStmtCacheSqlLimit", "2048"
        );
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public boolean createStatsTable(Stats stats) {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
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
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
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
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
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
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
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
        try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
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
            try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, player.getName());
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement ips = con.prepareStatement( "insert into `" + stats.getTableName() + "` (name, value) values (?, ?)")) {
                            ips.setString(1, player.getName());
                            ips.setString(2, value);
                            ips.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ups = con.prepareStatement("update `" + stats.getTableName() + "` set value = ? where name = ?")) {
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
