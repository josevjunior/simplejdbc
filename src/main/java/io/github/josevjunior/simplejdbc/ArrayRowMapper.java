
package io.github.josevjunior.simplejdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ArrayRowMapper implements RowMapper<Object[]>{

    @Override
    public Object[] map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException {
        Object[] result = new Object[mtdt.getColumnCount()];        
        
        for (int i = 0; i < mtdt.getColumnCount(); i++) {
            result[i] = resultSet.getObject(i+1);
        }
        
        return result;        
    }
    
}
