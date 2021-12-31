
package io.github.josevjunior.simplejdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {
    
    public static void close(Connection  c){
        try{
            if(c != null) {
                c.close();
            }
        }catch(SQLException e){}
    }
    
    public static void close(Statement s){
        try{
            if(s != null) {
                s.close();
            }
        }catch(SQLException e){}
    }
    
    public static void close(ResultSet r){
        try{
            if(r != null) {
                r.close();
            }
        }catch(SQLException e){}
    }
    
}
