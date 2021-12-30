
package io.github.josevjunior.simplejdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * The core interface which defines the contract to map a result to a specific T type
 */
public interface RowMapper<T> {
    
    public T map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException;
    
}
