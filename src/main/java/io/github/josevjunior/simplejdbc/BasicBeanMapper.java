
package io.github.josevjunior.simplejdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.RowProcessor;

/**
 * A basic bean mapper which uses the {@link org.apache.commons.dbutils.BasicRowProcessor} as
 * the mapper engine
 * @param <T> 
 */
public class BasicBeanMapper<T> implements RowMapper<T>{
    
    private final RowProcessor processor;
    private final Class<T> resultType;

    public BasicBeanMapper(Class<T> resultType) {
        this.processor = new BasicRowProcessor(new GenerousBeanProcessor());
        this.resultType = resultType;
    }

    @Override
    public T map(ResultSet resultSet, ResultSetMetaData mtdt) {
        try {
            return this.processor.toBean(resultSet, resultType);
        }catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
    
    
}
