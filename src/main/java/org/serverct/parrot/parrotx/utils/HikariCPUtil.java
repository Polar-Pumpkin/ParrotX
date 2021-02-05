package org.serverct.parrot.parrotx.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HikariCPUtil {

    private static final Map<PPlugin, HikariDataSource> sqlConnectionPool = new HashMap<>();

    public static void setSqlConnectionPool(final PPlugin plugin,
                                            final String address, final String port, final String database,
                                            final String username, final String password) {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");

        config.setConnectionTimeout(30000);
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(50);

        final String url = "jdbc:mysql://" + address + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setAutoCommit(true);

        sqlConnectionPool.put(plugin, new HikariDataSource(config));
    }

    public static Connection getConnection(final PPlugin plugin) {
        try {
            final HikariDataSource source = sqlConnectionPool.get(plugin);
            if (Objects.isNull(source)) {
                plugin.getLang().log.error(I18n.GET, "数据库链接", "未初始化 SQL 链接池.");
                return null;
            }
            return source.getConnection();
        } catch (SQLException e) {
            plugin.getLang().log.error(I18n.GET, "数据库链接", e, plugin.getPackageName());
        }
        return null;
    }

    public static void execute(@NotNull final PPlugin plugin, @NotNull final String sql,
                               @Nullable final String... args) {
        final Connection connection = getConnection(plugin);
        if (Objects.isNull(connection)) {
            return;
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            if (Objects.nonNull(args) && args.length > 0) {
                for (int index = 0; index < args.length; index++) {
                    statement.setString(index + 1, args[index]);
                }
            }
            statement.execute();
        } catch (SQLException exception) {
            plugin.getLang().log.error(I18n.EXECUTE, "MySQL 语句", exception, plugin.getPackageName());
        } finally {
            close(plugin, connection, statement, null);
        }
    }

    @Nullable
    public static ResultSet query(@NotNull final PPlugin plugin, @NotNull final String sql,
                                  @Nullable final String... args) {
        final Connection connection = getConnection(plugin);
        if (Objects.isNull(connection)) {
            return null;
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            if (Objects.nonNull(args) && args.length > 0) {
                for (int index = 0; index < args.length; index++) {
                    statement.setString(index + 1, args[index]);
                }
            }
            return statement.executeQuery();
        } catch (SQLException exception) {
            plugin.getLang().log.error(I18n.EXECUTE, "MySQL 语句", exception, plugin.getPackageName());
        } finally {
            close(plugin, connection, statement, null);
        }
        return null;
    }

    public static void close(@NotNull final PPlugin plugin,
                             @Nullable final Connection connection,
                             @Nullable final PreparedStatement statement,
                             @Nullable final ResultSet result) {
        try {
            if (Objects.nonNull(connection)) {
                connection.close();
            }
            if (Objects.nonNull(statement)) {
                statement.close();
            }
            if (Objects.nonNull(result)) {
                result.close();
            }
        } catch (final SQLException e) {
            plugin.getLang().log.error("关闭", "数据库链接", e, "serverct");
        }
    }

}
