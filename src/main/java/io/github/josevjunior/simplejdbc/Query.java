package io.github.josevjunior.simplejdbc;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A query object that wraps the jdbc statement execution. Each query handle a 
 * jdbc statement. Every operation at this class can throw a {@link io.github.josevjunior.simplejdbc.JdbcException}
 * and the source can be from a {@link java.sql.SQLException} or a internal exception
 * 
 * @param <T> The type of the query that influences {@link Query#getResultList() }, 
 * {@link Query#getFirstResult() } and {@link Query#getScrollableResult() } result
 */
public class Query<T> {

    private final QueryCreator queryCreator;
    private final NamedParameterSQL namedParameterSQL;
    private final PreparedStatement statement;
    private final RowMapper<T> mapper;

    public Query(NamedParameterSQL sql, PreparedStatement stam, QueryCreator creator, RowMapper<T> mapper) {
        this.queryCreator = creator;
        this.namedParameterSQL = sql;
        this.mapper = mapper;
        this.statement = stam;
    }

    /**
     * Set the parameter value
     * @param name The parameter name
     * @param value The parameter value
     * @return The query itself
     */
    public Query<T> setParameter(String name, Object value) {
        int[] indexes = this.namedParameterSQL.getParamIndex(name);
        
        if(indexes == null || indexes.length == 0) {
            throw new JdbcException("Parameter '" + name + "' not found");
        }
        
        setParameter(indexes, value);
        return this;
    }
    
    /**
     * Set the parameter value
     * @param i The parameter index 
     * @param value The parameter value
     * @return 
     */
    public Query<T> setParameter(int i, Object value) {
        setParameter(new int[] {i}, value);
        return this;
    }

    private void setParameter(int[] indexes, Object value) {
        try {
            if (value == null) {
                for (int index : indexes) {
                    this.statement.setNull(index, java.sql.Types.NULL);
                }
            } else {
                
                if(value instanceof Short) {
                    for (int index : indexes) {
                        this.statement.setShort(index, (short) value);
                    }                    
                } else if(value instanceof Integer) {
                    for (int index : indexes) {
                        this.statement.setInt(index, (int) value);
                    }
                }else if(value instanceof Double) {
                    for (int index : indexes) {
                        this.statement.setDouble(index, (double) value);
                    }
                }else if(value instanceof Float) {
                    for (int index : indexes) {
                        this.statement.setFloat(index, (float) value);
                    }
                }else if(value instanceof BigDecimal) {
                    for (int index : indexes) {
                        this.statement.setBigDecimal(index, (BigDecimal) value);
                    }
                }else if(value instanceof String) {
                    for (int index : indexes) {
                        this.statement.setString(index, (String) value);
                    }
                }else if(value instanceof Clob) {
                    for (int index : indexes) {
                        this.statement.setClob(index, (Clob) value);
                    }
                }else if(value instanceof Blob) {
                    for (int index : indexes) {
                        this.statement.setBlob(index, (Blob) value);
                    }                    
                }else if(value instanceof java.util.Date){
                    for (int index : indexes) {
                        this.statement.setTimestamp(index, new Timestamp(((java.util.Date)value).getTime()));
                    }
                }else if(value instanceof Timestamp){
                    for (int index : indexes) {
                        this.statement.setTimestamp(index, (Timestamp) value);
                    }
                }else if(value instanceof Date){
                    for (int index : indexes) {
                        this.statement.setDate(index, (Date) value);
                    }
                }else if(value instanceof Time){
                    for (int index : indexes) {
                        this.statement.setTime(index, (Time) value);
                    }
                } else {
                    for (int index : indexes) {
                        this.statement.setObject(index, value);
                    }
                }
                
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
    /**
     * Clear the setted parameters
     */
    public void clearParameters() {
        try {
            this.statement.clearParameters();
        }catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
    /**
     * Execute the update into database if the query is a DML statement
     * @return The updated rows count
     */
    public int executeUpdate() {
        try {
            boolean isASelect = this.statement.execute();
            if(isASelect) {
                throw new IllegalStateException("The query is not a DML statement");
            }
            
            return this.statement.getUpdateCount();
            
        }catch (SQLException | IllegalStateException e) {
            throw new JdbcException(e);
        }
    }
    
    /**
     * Execute the query and return the first result as a {@link java.util.Optional}
     * if the result does not exists the {@link java.util.Optional} will be empty
     * @return The first result as {@link java.util.Optional}
     */
    public Optional<T> getFirstResult() {
        
        ResultSet rs = null;
        try {
            rs = this.statement.executeQuery();
            if(rs.next()) {
                return Optional.of(mapper.map(rs, rs.getMetaData()));
            }
            
            return Optional.empty();
        }catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            JdbcUtils.close(rs);
        }
    }
    
    /**
     * Execute the query and return the result as a {@link java.util.List} with
     * all the row mapped for the class
     * @return a not null {@link java.util.List}
     */
    public List<T> getResultList() {
        
        List<T> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = this.statement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            
            while(rs.next()) {
                list.add(mapper.map(rs, metaData));
            }
            
            return list;
            
        }catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            JdbcUtils.close(rs);
        }
    }
    
    /**
     * Get the {@link java.sql.PreparedStatement} associated with this Query
     * @return a {@link java.sql.PreparedStatement}
     */
    public PreparedStatement getNativeStatement(){
        return statement;
    }
    
    /**
     * Execute the query and return the result as a {@link io.github.josevjunior.simplejdbc.ScrollableResult} 
     * with all the row mapped for the class
     * @return a not null {@link io.github.josevjunior.simplejdbc.ScrollableResult}
     */
    public ScrollableResult<T> getScrollableResult() {
        try {
            ResultSet rs = this.statement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            
            return new ScrollableResultImpl<T>(rs, metaData, mapper);
            
        }catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
    
    private static final class ScrollableResultImpl<T>  implements ScrollableResult<T> {
        
        private final ResultSet rs;
        private final ResultSetMetaData metaData;
        private final RowMapper<T> mapper;
        private final Map<Integer, T> cachedValues;

        public ScrollableResultImpl(ResultSet rs, ResultSetMetaData metaData, RowMapper<T> mapper) {            
            this.rs = rs;
            this.metaData = metaData;
            this.mapper = mapper;
            this.cachedValues = new HashMap<>();
        }
        
        @Override
        public T get() {
            try {
                T result = cachedValues.get(getRowId());
                if(result == null) {
                    result = mapper.map(rs, metaData);
                    cachedValues.put(getRowId(), result);
                }
                
                return result;
                
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public int getRowId() {
            try {
                return rs.getRow();
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public void beforeFirst() {
            try {
                rs.beforeFirst();
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public void afterLast() {
            try {
                rs.afterLast();
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public boolean goToRow(int rowId) {
            try {
                return rs.absolute(rowId);
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public boolean before() {
            try {
                return rs.previous();
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public boolean next() {
            try {
                return rs.next();
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public void close() throws Exception {
            rs.close();
        }
        
        
        
    }

}
