
package io.github.josevjunior.simplejdbc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class UpdateBuilder {
    
    private final Map<UpdateParam, Object> columnsAndValues;
    private final QueryCreator connection;
    private final String tableName;

    public UpdateBuilder(QueryCreator connection, String tableName) {
        this.columnsAndValues = new LinkedHashMap<>();
        this.connection = connection;
        this.tableName = tableName;
    }
    
    public UpdateBuilder set(String columnName, Object value) {
        columnsAndValues.put(new UpdateParam("K$_" + columnName, columnName), value);
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Map<UpdateParam, Object> getColumnsAndValues() {
        return columnsAndValues;
    }
    
    public UpdateBuilderCondition where() {
        return new UpdateBuilderCondition(this);
    }
    
    public static class UpdateBuilderCondition {
        
        private final Map<UpdateParam, Object> columnsAndValues;
        private final QueryCreator connection;        
        private final UpdateBuilder updateBuilder;

        public UpdateBuilderCondition(UpdateBuilder updateBuilder) {
            this.updateBuilder = updateBuilder;
            this.connection = updateBuilder.connection;
            columnsAndValues = new LinkedHashMap<>();
        }
        
        public UpdateBuilderCondition col(String name, Object column) {
            columnsAndValues.put(new UpdateParam("V$_" + name, name), column);
            return this;
        }        

        public Map<UpdateParam, Object> getColumnsAndValues() {
            return columnsAndValues;
        }

        public UpdateBuilder getUpdateBuilder() {
            return updateBuilder;
        }
        
        public int execute() {
            
            if(columnsAndValues.isEmpty()){
                throw new JdbcException("The update must have a WHERE condition");
            }
            
            SQLStatementBuilder stamBuilder = new SQLStatementBuilder();
            StringBuilder sbUpdate = stamBuilder.createUpdate(this);
            
            final Query query = connection.create(sbUpdate.toString());            
            BiConsumer<UpdateParam, Object> forEachCallback = (k, v) -> query.setParameter(k.getParamName(), v);
            
            getUpdateBuilder().getColumnsAndValues().forEach(forEachCallback);
            getColumnsAndValues().forEach(forEachCallback);
                        
            return query.executeUpdate();
        }
        
    }
    
    public static class UpdateParam {
        private final String paramName;
        private final String columnName;

        public UpdateParam(String paramName, String columnName) {
            this.paramName = paramName;
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getParamName() {
            return paramName;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.paramName);
            hash = 53 * hash + Objects.hashCode(this.columnName);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UpdateParam other = (UpdateParam) obj;
            if (!Objects.equals(this.paramName, other.paramName)) {
                return false;
            }
            if (!Objects.equals(this.columnName, other.columnName)) {
                return false;
            }
            return true;
        }
        
    }
    
}
