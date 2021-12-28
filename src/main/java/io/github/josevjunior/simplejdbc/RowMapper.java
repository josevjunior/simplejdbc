
package io.github.josevjunior.simplejdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * A core mapper interface
 * @author Jose
 */
public interface RowMapper<T> {
    
    public T map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException;
    
}
