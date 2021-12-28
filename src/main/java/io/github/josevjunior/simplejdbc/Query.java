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

public class Query<T> {

    private final QueryCreator connection;
    private final NamedParameterSQL namedParameterSQL;
    private final PreparedStatement statement;
    private final RowMapper<T> mapper;

    public Query(NamedParameterSQL sql, PreparedStatement stam, QueryCreator connection, RowMapper<T> mapper) {
        this.connection = connection;
        this.namedParameterSQL = sql;
        this.mapper = mapper;
        this.statement = stam;
    }

    public Query<T> setParameter(String name, Object value) {
        int[] indexes = this.namedParameterSQL.getParamIndex(name);
        setParameter(indexes, value);
        return this;
    }
    
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
    
    public void clearParameters() {
        try {
            this.statement.clearParameters();
        }catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
    public int executeUpdate() {
        try {
            return this.statement.executeUpdate();
        }catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
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
    
    public List<T> getResultList() {
        
        List<T> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = this.statement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            
            while(rs.next()) {
                list.add(mapper.map(rs, metaData));
            };
            
            return list;
            
        }catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            JdbcUtils.close(rs);
        }
    }
    
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
        public boolean goToFirst() {
            try {
                return rs.first();
            }catch(SQLException e) {
                throw new JdbcException(e);
            }
        }

        @Override
        public boolean goToLast() {
            try {
                return rs.last();
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
