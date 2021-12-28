
package io.github.josevjunior.simplejdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper which contains all information about the connection (statements, configs, etc)
 * This object will may be bind per thread thanks to ThreadResourceManager
 */
public class ConnectionSession {
    
    private final Connection connection;
    private final Set<PreparedStatement> statements;

    public ConnectionSession(Connection connection) {
        this.connection = connection;
        this.statements = new HashSet<>();
    }
    
    public PreparedStatement prepareStatement(String sql) {
        try {
            PreparedStatement stam = StatementProxy.proxy(connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
            statements.add(stam);
            return stam;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }
    
    public void closeSession() {
        for (PreparedStatement stam : statements) {
            try{
                stam.close();
            }catch(Exception ignored) {}
        }
        
        try {
            connection.close();
        }catch(Exception ignored) {}
        
    }
    
    private static class StatementProxy implements InvocationHandler{

        private final Statement stam;
        private final Set<ResultSet> resultSets;

        public StatementProxy(Statement stam) {
            this.stam = stam;
            this.resultSets = new HashSet<>();
        }
        
        public static PreparedStatement proxy(PreparedStatement stam) {
            return (PreparedStatement) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { Statement.class }, new StatementProxy(stam));
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                
                if("close".equals(method.getName())) {
                    for (ResultSet resultSet : resultSets) {
                        try {
                            resultSet.close();
                        }catch (Exception e) {}
                        
                    }
                }
                
                Object result = method.invoke(stam, args);
                if(result != null && result instanceof ResultSet) {
                    this.resultSets.add((ResultSet) result);
                }
                
                return result;
                
            }catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
        
    }
    
    
}
