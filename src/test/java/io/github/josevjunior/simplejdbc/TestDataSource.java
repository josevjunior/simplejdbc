
package io.github.josevjunior.simplejdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class TestDataSource {
    
    private static DataSource dataSource;
    private static boolean firstConnection = true;
    
    public static DataSource buildTestDataSource() {
        
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        
        return ds;                
    }

    public static DataSource getDataSource() {
        if(dataSource == null) {
            dataSource = buildTestDataSource();
            
            try(Connection c = getNewConnection()){
                // Para executar os scripts iniciais
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        return dataSource;
    }
    
    
    
    public static Connection getNewConnection() throws Exception{
        
        Connection connection = getDataSource().getConnection();
        
        connection.setAutoCommit(true);
        if(firstConnection) {
            
            firstConnection = false;
            
            connection.setAutoCommit(false);
            
            SQLFileReader sqlFileReader = new SQLFileReader("/scripts/init.sql");
            for (String statement : sqlFileReader.getStatements()) {
                connection.createStatement().executeUpdate(statement);
            }
            
            connection.commit();            
            connection.setAutoCommit(true);
            
        }
        return connection;
    }
    
    private static class SQLFileReader {

        private final String filePath;

        public SQLFileReader(String filePath) {
            this.filePath = filePath;            
        }
        
        
        public List<String> getStatements() {
            List<String> stams = new ArrayList<>();
            InputStream is = getClass().getResourceAsStream(filePath);
            
            StringBuilder sb = new StringBuilder();
            try(Scanner scanner = new Scanner(is)){
                while(scanner.hasNextLine()){
                    String nextLine = scanner.nextLine();
                    sb.append(nextLine);
                    if(nextLine.contains(";")) {
                        stams.add(sb.toString());
                        sb.setLength(0);
                    }
                }
            }
            
            return stams;
        }
        
    }
    
}
