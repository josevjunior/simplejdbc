
package io.github.josevjunior.simplejdbc;

import io.github.josevjunior.simplejdbc.Query;
import io.github.josevjunior.simplejdbc.QueryCreator;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DMLQueryTest {
    
    private boolean inserted = false;
    
    @Test
    public void testInsertRecordInTableWithDML() {
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO TEST_TABLE(");
        sql.append("CHAR_COL, VARCHAR_COL, TINYINT_COL, INTEGER_COL, BIGINT_COL, ");
        sql.append("NUMERIC_COL, REAL_COL, DOUBLE_COL, DATE_COL, TIMESTAMP_COL, ");
        sql.append("TIME_COL) ");
        sql.append("VALUES( ");
        sql.append(":CHAR_COL, :VARCHAR_COL, :TINYINT_COL, :INTEGER_COL, :BIGINT_COL, ");
        sql.append(":NUMERIC_COL, :REAL_COL, :DOUBLE_COL, :DATE_COL, :TIMESTAMP_COL, ");
        sql.append(":TIME_COL) ");
        
        
        QueryCreator qc = new QueryCreator(TestDataSource.getDataSource());
        Query query = qc.create(sql.toString());
        query.setParameter("CHAR_COL", '1');
        query.setParameter("VARCHAR_COL", "2");
        query.setParameter("TINYINT_COL", 3);
        query.setParameter("INTEGER_COL", 4);
        query.setParameter("BIGINT_COL", 5);
        query.setParameter("NUMERIC_COL", 6);
        query.setParameter("REAL_COL", 7);
        query.setParameter("DOUBLE_COL", 8);
        query.setParameter("DATE_COL", new Date(System.currentTimeMillis()));
        query.setParameter("TIMESTAMP_COL", new Timestamp(System.currentTimeMillis()));
        query.setParameter("TIME_COL", new Time(System.currentTimeMillis()));
        
        int updatedRows = query.executeUpdate();
        Assert.assertTrue(updatedRows > 0);
        
        inserted = true;
    }
    
    @Test
    public void testUpdateRecordInTableWithDML() {
        
        if(!inserted) {
            testInsertRecordInTableWithDML();
        }
        
        QueryCreator qc = new QueryCreator(TestDataSource.getDataSource());
        Query update = qc.create("UPDATE TEST_TABLE SET VARCHAR_COL = :NEW_VALUE WHERE INTEGER_COL = :INTEGER_COL ");
        
        update.setParameter("NEW_VALUE", "UPDATE VARCHAR");
        update.setParameter("INTEGER_COL", 4);
        
        int updatedRows = update.executeUpdate();
        
        Assert.assertEquals(updatedRows, 2);
        
        List<Object[]> select = qc.create("SELECT VARCHAR_COL FROM TEST_TABLE WHERE INTEGER_COL = :INTEGER_COL")
                .setParameter("INTEGER_COL", 4)
                .getResultList();
        
        Assert.assertEquals("UPDATE VARCHAR", select.get(0)[0]);
        
    }
    
}
