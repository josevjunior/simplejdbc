
package io.github.josevjunior.simplejdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Holds all default basic type mappers for single column queries
 */
public final class ColumnValueMappers {
    
    /**
     * A mapper that gets the first column value as {@link java.lang.Character}.
     * <br>
     * As the JDBC ResultSet doesnt have a character getter, the value is accessed
     * using {@link java.sql.ResultSet#getString} and then parsed as charecter if the 
     * content contains only one character. Otherwise, a {@link java.lang.IllegalStateException}
     * is thrown.
     */
    public static final RowMapper<Character> CHAR_COLUMN_VALUE_MAPPER = (rs, md) -> {
        String str = rs.getString(1);
        
        if(str == null) {
            return null;
        }
        
        if(str.length() == 1) {
            return str.charAt(0);
        }
        
        throw new IllegalStateException("The column cannot be converter to char becouse contains more than one digit");
    };
    
    
    /**
     * A mapper that gets the first column value as {@link java.lang.String}.
     */    
    public static final RowMapper<String> STRING_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getString(1);
    
    /**
     * A mapper that gets the first column value as {@link java.lang.Short}.
     */
    public static final RowMapper<Short> SHORT_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getShort(1);
    
    /**
     * A mapper that gets the first column value as {@link java.lang.Integer}.
     */
    public static final RowMapper<Integer> INTEGER_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getInt(1);
    
    /**
     * A mapper that gets the first column value as {@link java.lang.Long}.
     */
    public static final RowMapper<Long> LONG_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getLong(1);
    
    /**
     * A mapper that gets the first column value as {@link java.lang.Float}.
     */
    public static final RowMapper<Float> FLOAT_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getFloat(1);
    
    /**
     * A mapper that gets the first column value as {@link java.lang.Double}.
     */
    public static final RowMapper<Double> DOUBLE_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getDouble(1);
    
    /**
     * A mapper that gets the first column value as {@link java.math.BigDecimal}.
     */
    public static final RowMapper<BigDecimal> BIGDECIMAL_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getBigDecimal(1);
    
    /**
     * A mapper that gets the first column value as {@link java.math.BigInteger}.
     */
    public static final RowMapper<BigInteger> BIGINTEGER_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getBigDecimal(1) == null ? null : rs.getBigDecimal(1).toBigInteger();
    
    /**
     * A mapper that gets the first column value as {@link java.sql.Blob}.
     */
    public static final RowMapper<Blob> BLOB_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getBlob(1);
    
    /**
     * A mapper that gets the first column value as {@link java.sql.Clob}.
     */
    public static final RowMapper<Clob> CLOB_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getClob(1);
    
    /**
     * A mapper that gets the first column value as {@link java.sql.Timestamp}.
     */
    public static final RowMapper<Timestamp> TIMESTAMP_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getTimestamp(1);
        
    /**
     * A mapper that gets the first column value as {@link java.sql.Date}.
     */
    public static final RowMapper<Date> DATE_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getDate(1);
    
    /**
     * A mapper that gets the first column value as {@link java.sql.Time}.
     */
    public static final RowMapper<Time> TIME_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getTime(1);
    
    
    /**
     * A mapper that gets the first column value as {@link java.util.Date}.
     */
    public static final RowMapper<java.util.Date> DATE_UTIL_COLUMN_VALUE_MAPPER = (rs, md) -> rs.getTimestamp(1);
    
    
    
}
