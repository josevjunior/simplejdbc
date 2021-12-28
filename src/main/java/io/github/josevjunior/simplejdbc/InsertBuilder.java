
package io.github.josevjunior.simplejdbc;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertBuilder {

    private final QueryCreator queryCreator;
    private final Map<String, Object> columnsAndValues;
    private final String tableName;

    public InsertBuilder(QueryCreator queryCreator, String tableName) {
        this.tableName = tableName;
        this.queryCreator = queryCreator;
        this.columnsAndValues = new LinkedHashMap<>();
    }
    
    public InsertBuilder col(String colName, Object colValue) {
        columnsAndValues.put(colName, colValue);
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, Object> getColumnsAndValues() {
        return columnsAndValues;
    }
    
    public void execute() {
        
        SQLStatementBuilder sqlBuilder = new SQLStatementBuilder();
        StringBuilder sql = sqlBuilder.createInsert(this);
        
        final Query<Object[]> query = queryCreator.create(sql.toString());
        columnsAndValues.forEach((k, v) -> query.setParameter(k, v));
        query.executeUpdate();        
        
    }
    
    
    
}
