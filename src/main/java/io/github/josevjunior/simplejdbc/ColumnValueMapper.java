
package io.github.josevjunior.simplejdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ColumnValueMapper<T> implements RowMapper<T>{

    @Override
    public T map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException {
        return (T) resultSet.getObject(1);
    }
    
}
