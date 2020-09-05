package org.serverct.parrot.parrotx.utils.sql.statement;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL 语句执行类。
 * @author 洋洋
 * @since 1.3.2
 */

public class StatementExecutor {

    private final Connection connection;
    @Getter
    private PreparedStatement statement;
    @Getter
    private ResultSet resultSet;

    public StatementExecutor(Connection connection) {
        this.connection = connection;
    }

    public PreparedStatement executeUpdate(String sql, String... param) throws SQLException {
        statement = connection.prepareStatement(sql);
        int index = 1;
        for (String s : param) {
            statement.setString(index, s);
            index++;
        }
        return statement;
    }

    public ResultSet executeQuery(String sql, String... param) throws SQLException {
        PreparedStatement preparedStatement = executeUpdate(sql, param);
        return resultSet = preparedStatement.executeQuery();
    }
}
