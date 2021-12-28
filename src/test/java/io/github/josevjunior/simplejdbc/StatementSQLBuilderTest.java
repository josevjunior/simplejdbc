
package io.github.josevjunior.simplejdbc;

import io.github.josevjunior.simplejdbc.SQLStatementBuilder;
import io.github.josevjunior.simplejdbc.UpdateBuilder;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class StatementSQLBuilderTest {
    
    @Test
    public void testUpdateStatement() {
        
        UpdateBuilder.UpdateBuilderCondition condition = new UpdateBuilder(null, "USERS")
                .set("ID", 1)
                .set("NAME", "ZJOSÉ")
                .set("BIRTHDAY", new Date())
                .set("SALARY", 500)
                .set("GENRE", "M")
                .where()
                .col("NAME", "JOSÉ");
        
        SQLStatementBuilder sqlBuilder = new SQLStatementBuilder();
        
        Assert.assertNotNull("Not Null", sqlBuilder.createUpdate(condition));
        System.out.println(sqlBuilder.createUpdate(condition));
        
    }
    
}
