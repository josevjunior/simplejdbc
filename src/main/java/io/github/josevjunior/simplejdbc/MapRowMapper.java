
package io.github.josevjunior.simplejdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A mapper that converts a resultset row in a {@link java.util.HashMap}
 * <br>
 * The column name will be used as map key. So, is mandatory the sql contains
 * accessible alias for the return columns
 */
public class MapRowMapper implements RowMapper<Map<String, Object>>{

    @Override
    public Map<String, Object> map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException {
        
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < mtdt.getColumnCount(); i++) {
            map.put(mtdt.getColumnName(i+1), resultSet.getObject(i+1));
        }
        
        return map;
        
    }
    
}
