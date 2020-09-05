package org.serverct.parrot.parrotx.utils.sql;

import org.serverct.parrot.parrotx.utils.sql.statement.StatementExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL 链接工具类接口。
 *
 * @author 洋洋
 * @since 1.3.2
 */

public interface IConnection {

    void connect(String ip, String user, String pass, String port, String table, String options) throws ClassNotFoundException, SQLException;

    Connection getConnection() throws SQLException;

    StatementExecutor getStatementExecutor();

    PreparedStatement getStatement();

    ResultSet getResultSet();

    PreparedStatement executeSql(String sql, String... param) throws Exception;

    ResultSet querySql(String sql, String... param) throws Exception;

    boolean existsByKeyId(String table, String field, String value) throws Exception;

    void close() throws Exception;

}
