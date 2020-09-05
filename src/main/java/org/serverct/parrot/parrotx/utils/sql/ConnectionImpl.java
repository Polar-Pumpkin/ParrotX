package org.serverct.parrot.parrotx.utils.sql;

import org.serverct.parrot.parrotx.utils.sql.statement.StatementExecutor;

import java.sql.*;

/**
 * SQL 链接实现类。
 *
 * @author 洋洋
 * @since 1.3.2
 */

public class ConnectionImpl implements IConnection {

    private String ip;
    private String user;
    private String pass;
    private String port;
    private String table;
    private String driver;
    private String options;

    private Connection connection;
    private StatementExecutor statement;

    @Override
    public void connect(String ip, String user, String pass, String port, String table, String options) throws ClassNotFoundException, SQLException {
        this.ip = ip;
        this.user = user;
        this.pass = pass;
        this.port = port;
        this.table = table;
        this.options = options;
        Class.forName("com.mysql.jdbc.Driver");
        driver = "jdbc:mysql://" + ip + ":" + port + "/" + table;
        if (options != null) {
            driver = driver + "?" + options;
        }
        getConnection();
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(driver, user, pass);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = createConnection();
            statement = new StatementExecutor(connection);
        }
        return connection;
    }

    @Override
    public StatementExecutor getStatementExecutor() {
        return statement;
    }

    @Override
    public PreparedStatement getStatement() {
        return statement.getStatement();
    }

    @Override
    public ResultSet getResultSet() {
        return statement.getResultSet();
    }

    @Override
    public PreparedStatement executeSql(String sql, String... param) throws Exception {
        return statement.executeUpdate(sql, param);
    }

    @Override
    public ResultSet querySql(String sql, String... param) throws Exception {
        return statement.executeQuery(sql, param);
    }

    @Override
    public boolean existsByKeyId(String table, String field, String value) throws Exception {
        return querySql("select 1 from ? where ?=? ", table, field, value).next();
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
