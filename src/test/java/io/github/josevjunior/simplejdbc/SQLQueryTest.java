
package io.github.josevjunior.simplejdbc;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SQLQueryTest {
    
    private static boolean inserted = false;
    
    private static int index = 0;
    
    
    @Test
    public void testInsertNewRecord() {
        
        System.out.println(this);
        
        index = index - 1;
        
        QueryCreator qc = new QueryCreator(TestDataSource.getDataSource());
        qc.insert("TEST_TABLE")
                .col("ID", index)
                .col("CHAR_COL", 'C')
                .col("VARCHAR_COL", "Variable Length Text")
                .col("TINYINT_COL", new Short("1"))
                .col("INTEGER_COL", 2)
                .col("BIGINT_COL", 3L)
                .col("NUMERIC_COL", new BigDecimal("4.44"))
                .col("REAL_COL", 5.555f)
                .col("DOUBLE_COL", 5.555)
                .col("DATE_COL", new Date(System.currentTimeMillis()))
                .col("TIMESTAMP_COL", new Timestamp(System.currentTimeMillis()))
                .col("TIME_COL", new Time(System.currentTimeMillis()))
                .execute();
        
        List<Integer> columnValues = getColumnValues("INTEGER_COL", Integer.class);
        assertEquals(columnValues.size() > 0, true);
        
        inserted = true;
    }
    
    @Test
    public void testQuerySingleDateColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
         List<Date> values = getColumnValues("DATE_COL", Date.class);
        for (Date columnValue : values) {
            assertTrue(columnValue.before(new Date(System.currentTimeMillis())));
        }
        
    }
    
    @Test
    public void testQuerySingleTimeColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
         List<Time> values = getColumnValues("TIME_COL", Time.class);
        for (Time columnValue : values) {
            assertTrue(columnValue.before(new Time(System.currentTimeMillis())));
        }
        
    }
    
    @Test
    public void testQuerySingleTimestampColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
         List<Timestamp> values = getColumnValues("TIMESTAMP_COL", Timestamp.class);
        for (Timestamp columnValue : values) {
            assertTrue(columnValue.before(new Timestamp(System.currentTimeMillis())));
        }
        
    }
    
    @Test
    public void testQuerySingleFloatColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
         List<Float> values = getColumnValues("REAL_COL", Float.class);
        for (Float columnValue : values) {
            assertEquals(columnValue, new Float(5.555f));
        }
        
    }
    
    @Test
    public void testQuerySingleStringColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
         List<String> stringValues = getColumnValues("VARCHAR_COL", String.class);
        for (String columnValue : stringValues) {
            assertEquals(columnValue, "Variable Length Text");
        }
        
    }
    
    @Test
    public void testQuerySingleShortColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
        List<Short> shortValues = getColumnValues("TINYINT_COL", Short.class);
        for (Short columnValue : shortValues) {
            assertEquals(columnValue, new Short("1"));
        }
        
    }
    
    @Test
    public void testQuerySingleIntegerColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
        List<Integer> intValues = getColumnValues("INTEGER_COL", Integer.class);
        for (Integer columnValue : intValues) {
            assertEquals(columnValue, Integer.valueOf(2));
        }
        
    }
    
    @Test
    public void testQuerySingleLongColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
        List<Long> longValues = getColumnValues("BIGINT_COL", Long.class);
        for (Long columnValue : longValues) {
            assertEquals(columnValue, Long.valueOf(3));
        }
        
    }
    
    @Test
    public void testQuerySingleBigDecimalColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
         List<BigDecimal> decimalValues = getColumnValues("NUMERIC_COL", BigDecimal.class);
        for (BigDecimal columnValue : decimalValues) {
            assertTrue(columnValue.compareTo(new BigDecimal("4.44")) == 0);
        }
        
    }
    
    @Test
    public void testQuerySingleCharColumn() {
        
        if(!inserted) {
            testInsertNewRecord();
        }
        
        List<Character> charValues = getColumnValues("CHAR_COL", Character.class);
        for (Character columnValue : charValues) {
            assertEquals(columnValue, Character.valueOf('C'));
        }
        
    }
    
    private <T> List<T> getColumnValues(String columnName, Class<T> clazz){
        
        QueryCreator c = new QueryCreator(TestDataSource.getDataSource());
        Query<T> q = c.create("SELECT "+ columnName +" FROM TEST_TABLE WHERE ID = ?", clazz);
        q.setParameter(1, -1);
        return q.getResultList();
    }
    
}
