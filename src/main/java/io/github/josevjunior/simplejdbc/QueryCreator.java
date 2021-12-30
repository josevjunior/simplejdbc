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

/**
 * The main entrypoint of the mapper functionality. A QueryCreator is always 
 * bind to a {@link java.sql.Connection} that can be passed as a {@link #QueryCreator(java.sql.Connection)} 
 * argument or abtained by a {@link javax.sql.DataSource} through the {@link #QueryCreator(javax.sql.DataSource)} 
 * method
 * <br>
 * As the object is associated to a resource, you may need dispose it after use.
 * If you create the object through the Datasource constructor, the {@link #closeConnection()}
 * call is mandatory, otherwise you only should call the {@link #disposeResources()} 
 * method
 */
public class QueryCreator implements AutoCloseable {
    
    /**
     * The default row mapper inject. Always create a new BasicBeanMapper
     */
    public static final RowMapperInjector DEFAULT_ROW_MAPPER_INJECTOR = (cls) -> new BasicBeanMapper<>(cls);

    private static Map<Class, RowMapper<?>> mappers = new HashMap<Class, RowMapper<?>>();
    private static RowMapperInjector defaultMapperInjector = DEFAULT_ROW_MAPPER_INJECTOR;

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

    /**
     * Defines the default mapper that will always be used to a type
     * <br>
     * e.g:
     * <pre>{@code 
        QueryCreator.setDefaultTypeMapper(Json.class, new JsonMapper());
        ...
        QueryCreator queryCreator = new QueryCreator(connection);
        Query<Json> query = queryCreator.create("SELECT * FROM MY_TABLE", Json.class);
        query.getResultList(); // The result will be a list of json
  
        }</pre>
     * 
     * A default type mapper can be removed if the mapper value is set as null
     * <br>
     * e.g:
     * <br>
     * {@code QueryCreator.setDefaultTypeMapper(Json.class, null);}
     * 
     * 
     * @param clazz The class which will be associated to the mapper
     * @param mapper The mapper
     */
    public static void setDefaultTypeMapper(Class clazz, RowMapper mapper) {
        if(mapper == null){
            mappers.remove(clazz);
            return;
        }
        mappers.put(clazz, mapper);
    }

    /**
     * Get the registered default mapper for a {@link java.lang.Class}
     * @param clazz A class
     * @return The default mapper for the class or null if not exists
     */
    public static RowMapper getDefaultMapper(Class clazz) {
        return mappers.get(clazz);
    }

    /**
     * Change the default RowMapperInjector used when a mapper is not found
     * at default type mappers
     * <br>
     * e.g:
     * <br>
     * <pre>{@code 
     *  QueryCreator queryCreator = new QueryCreator(connection);
     *  Query<Employee> query = queryCreator.create("SELECT * FROM EMPLOYEE", Employee.class);
     * }</pre>
     * 
     * At the example above, does not exist a mapper for the Employee type. So 
     * the DefaultMapperInjector will be used to create a new generic row mapper
     * for the type
     * 
     * @param defaultMapperInjector A RowMapperInject implementation or null to
     * reset to the {@link #DEFAULT_ROW_MAPPER_INJECTOR} value
     */
    public static void setDefaultMapperInjector(RowMapperInjector defaultMapperInjector) {
        if(defaultMapperInjector != null) {
            QueryCreator.defaultMapperInjector = defaultMapperInjector;
        } else {
            QueryCreator.defaultMapperInjector = DEFAULT_ROW_MAPPER_INJECTOR;
        }
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
    
    /**
     * Create a {@link Query} with the given sql and associated it with the {@link io.github.josevjunior.simplejdbc.RowMapper}
     * @param sql The database sql
     * @param rowMapper The mapper
     * @return A Query object
     */
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

    /**
     * Create a {@link Query} with the given sql. The mapper will be discovered
     * through the resultClass param. If the class does not have a associeted mapper
     * will be used the default bean mapper {@link io.github.josevjunior.simplejdbc.BasicBeanMapper}
     * @param sql The database sql
     * @param rowMapper The mapper
     * @return A Query object
     */
    public <T> Query<T> create(String sql, Class<T> resultClass) {
        return create(sql, getRowMapperForClass(resultClass));
    }
    
    /**
     * Create a {@link Query} with the given sql and the {@link io.github.josevjunior.simplejdbc.ArrayRowMapper}
     * as row mapper
     * @param rowMapper The mapper
     * @return A Query object
     */
    public Query<Object[]> create(String sql) {
        return create(sql, new ArrayRowMapper());
    }

    private <T> RowMapper<T> getRowMapperForClass(Class<T> clazz) {
        RowMapper<T> mapper = (RowMapper<T>) mappers.get(clazz);
        if (mapper == null) {
            mapper = defaultMapperInjector.inject(clazz);
        }

        return mapper;
    }

    private PreparedStatement getNativeStatement(String sql) throws SQLException {
        return this.connection.prepareStatement(sql);
    }

    /**
     * Inits a update statement builder
     * @param table The table which will be updated
     * @return A {@link UpdateBuilder}
     */
    public UpdateBuilder update(String table) {
        return new UpdateBuilder(this, table);
    }
    
    /**
     * Inits a insert statement builder
     * @param table The table which will be inserted to
     * @return A {@link InsertBuilder}
     */
    public InsertBuilder insert(String table) {
        return new InsertBuilder(this, table);
    }
    
    public void closeConnection(){
        
        disposeResources();
        
        try{
            connection.close();
        }catch(Exception e){}
    }
    
    /**
     * Close all resources obtained by this QueryCreator
     */
    public void disposeResources(){
        for (PreparedStatement statement : statements) {
            try{
                statement.close();
            }catch(Exception ignored){}
        }
    }

    public void close() {
        closeConnection();
    }
    
    /**
     * Get the native {@link java.sql.Connection}
     */
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
