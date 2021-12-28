
package io.github.josevjunior.simplejdbc;

import java.util.Iterator;
import java.util.Map;

public class SQLStatementBuilder {
    
    public StringBuilder createUpdate(UpdateBuilder.UpdateBuilderCondition condition) {

        UpdateBuilder updateBuilder = condition.getUpdateBuilder();
        
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(updateBuilder.getTableName());
        sql.append(" SET ");
        
        for (Iterator iterator = updateBuilder.getColumnsAndValues().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<UpdateBuilder.UpdateParam, Object> entry = (Map.Entry<UpdateBuilder.UpdateParam, Object>) iterator.next();
            sql.append(entry.getKey().getColumnName()).append(" = :").append(entry.getKey().getParamName());
            if(iterator.hasNext()) {
                sql.append(", ");
            }
        }
        
        sql.append(" WHERE ");
        
        for (Iterator iterator = condition.getColumnsAndValues().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<UpdateBuilder.UpdateParam, Object> entry = (Map.Entry<UpdateBuilder.UpdateParam, Object>) iterator.next();
            sql.append(entry.getKey().getColumnName()).append(" = :").append(entry.getKey().getParamName());
            if(iterator.hasNext()) {
                sql.append(" AND ");
            }
        }
        
        return sql;
        
    } 
    
    
    public StringBuilder createInsert(InsertBuilder insertBuilder) {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(insertBuilder.getTableName());
        sql.append(" (");
        
        for (Iterator iterator = insertBuilder.getColumnsAndValues().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
            sql.append(entry.getKey());
            if(iterator.hasNext()) {
                sql.append(", ");
            }
        }
        
        sql.append(")VALUES (");
        
        for (Iterator iterator = insertBuilder.getColumnsAndValues().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
            sql.append(" :").append(entry.getKey());
            if(iterator.hasNext()) {
                sql.append(", ");
            }
        }
        
         sql.append(") ");
        
        return sql;
        
    } 
    
}
