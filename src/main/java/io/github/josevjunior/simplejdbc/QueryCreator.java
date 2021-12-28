package io.github.josevjunior.simplejdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;

public class QueryCreator implements AutoCloseable {

    private static Map<Class, RowMapper<?>> mappers = new HashMap<Class, RowMapper<?>>();

    static {
        mappers.put(Short.class, ColumnValueMappers.SHORT_COLUMN_VALUE_MAPPER);
        mappers.put(Integer.class, ColumnValueMappers.INTEGER_COLUMN_VALUE_MAPPER);
        mappers.put(Long.class, ColumnValueMappers.LONG_COLUMN_VALUE_MAPPER);
        mappers.put(Double.class, ColumnValueMappers.DOUBLE_COLUMN_VALUE_MAPPER);
        mappers.put(Float.class, ColumnValueMappers.FLOAT_COLUMN_VALUE_MAPPER);
        mappers.put(String.class, ColumnValueMappers.STRING_COLUMN_VALUE_MAPPER);
        mappers.put(Character.class, ColumnValueMappers.CHAR_COLUMN_VALUE_MAPPER);
        mappers.put(BigDecimal.class, ColumnValueMappers.BIGDECIMAL_COLUMN_VALUE_MAPPER);
        mappers.put(BigInteger.class, ColumnValueMappers.BIGINTEGER_COLUMN_VALUE_MAPPER);
        mappers.put(Clob.class, ColumnValueMappers.CLOB_COLUMN_VALUE_MAPPER);
        mappers.put(Blob.class, ColumnValueMappers.BLOB_COLUMN_VALUE_MAPPER);
        mappers.put(Timestamp.class, ColumnValueMappers.TIMESTAMP_COLUMN_VALUE_MAPPER);
        mappers.put(Time.class, ColumnValueMappers.TIME_COLUMN_VALUE_MAPPER);
        mappers.put(Date.class, ColumnValueMappers.DATE_COLUMN_VALUE_MAPPER);
        mappers.put(java.util.Date.class, ColumnValueMappers.DATE_UTIL_COLUMN_VALUE_MAPPER);
        mappers.put(Object[].class, new ArrayRowMapper());
        mappers.put(Map.class, new MapRowMapper());
    }

    private final Connection connection;
    private final Set<PreparedStatement> statements;
    private boolean isFromConnection = false;

    public static void registerDefaultMapper(Class clazz, RowMapper mapper) {
        mappers.put(clazz, mapper);
    }

    public static RowMapper getDefaultMapper(Class clazz) {
        return mappers.get(clazz);
    }

    public QueryCreator(DataSource dataSource) {
        try{            
           this.connection = dataSource.getConnection();
        }catch (SQLException e){
            throw new JdbcException(e);
        }
        this.statements = new HashSet<>();
    }
    
    public QueryCreator(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Connection should not be null!");
        this.statements = new HashSet<>();
    }
    
    public <T> Query<T> create(String sql, RowMapper<T> rowMapper) {
        try {
            NamedParameterSQL namedParemetSQL = NamedParameterSQL.parse(sql);
            PreparedStatement stam = getNativeStatement(namedParemetSQL.getParsedQuery());
            statements.add(stam);

            return new Query<T>(namedParemetSQL, stam, this, rowMapper);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public <T> Query<T> create(String sql, Class<T> resultClass) {
        return create(sql, getRowMapperForClass(resultClass));
    }

    private <T> RowMapper<T> getRowMapperForClass(Class<T> clazz) {
        RowMapper<T> mapper = (RowMapper<T>) mappers.get(clazz);
        if (mapper == null) {
            mapper = new BasicBeanMapper<T>(clazz);
        }

        return mapper;
    }

    private PreparedStatement getNativeStatement(String sql) throws SQLException {
        return this.connection.prepareStatement(sql);
    }

    public Query<Object[]> create(String sql) {
        return create(sql, Object[].class);
    }
    
    public UpdateBuilder update(String table) {
        return new UpdateBuilder(this, table);
    }
    
    public InsertBuilder insert(String table) {
        return new InsertBuilder(this, table);
    }
    
    public void closeConnection(){
        for (PreparedStatement statement : statements) {
            try{
                statement.close();
            }catch(Exception ignored){}
        }
        
        try{
            connection.close();
        }catch(Exception e){}
        
        if(isFromConnection) {
            ResourceManager.remove(this.connection);
        }
    }

    public void close() {
        closeConnection();
    }
    
    public Connection getNativeConnection() {
        return connection;
    }
    
    public void commit() {
        try{
            connection.commit();
        }catch(SQLException e) {
            throw new JdbcException(e);
        }
        
    }
    
    public void rollback() {
        try{
            connection.rollback();
        }catch(SQLException e) {
            throw new JdbcException(e);
        }    
    }

}
