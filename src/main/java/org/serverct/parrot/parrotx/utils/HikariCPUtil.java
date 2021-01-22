package org.serverct.parrot.parrotx.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariCPUtil {

    private static HikariDataSource sqlConnectionPool;

    public static void setSqlConnectionPool(final String address, final String port, final String database,
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

        sqlConnectionPool = new HikariDataSource(config);
    }

    public Connection getConnection(final PPlugin plugin) {
        try {
            return sqlConnectionPool.getConnection();
        } catch (SQLException e) {
            plugin.getLang().log.error(I18n.GET, "数据库链接", e, plugin.getPackageName());
        }
        return null;
    }

}
