
package io.github.josevjunior.simplejdbc;

import io.github.josevjunior.simplejdbc.QueryCreator;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class QueryFactoryTest {
    
    private static DataSource dataSource;
    
    @BeforeClass
    public static void beforeAll() {
        dataSource = TestDataSource.buildTestDataSource();
    }
    
    @Test
    public void shouldCreateNewConnection() {
        QueryCreator q1 = new QueryCreator(dataSource);
        QueryCreator q2 = new QueryCreator(dataSource);
        
        assertNotNull("Connection should've been created!", q1.getNativeConnection());        
        assertNotEquals("Connections should be diferents!", q1.getNativeConnection(), q2.getNativeConnection());
    }
    
    
    @Test
    public void shouldNotCreateNewConnection() throws Exception{
        
        Connection connection = dataSource.getConnection();
        
        QueryCreator q1 = new QueryCreator(connection);        
        QueryCreator q2 = new QueryCreator(connection);
        
        assertEquals("Connection should've not been created!", q1.getNativeConnection(), q2.getNativeConnection());
    }
    
}
