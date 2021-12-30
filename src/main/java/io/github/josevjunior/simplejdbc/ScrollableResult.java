
package io.github.josevjunior.simplejdbc;

/**
 * A wrapper that provides a way to navigate through resultset without 
 * necessarily map all the records before that. The advanced method navigation
 * are jdbc driver dependent
 * <br>
 * All methods can throw a {@link io.github.josevjunior.simplejdbc.JdbcException} 
 * <br>
 * <br>
 * <b>Note: </b> Thre ScrollableResult holds a database resource, so is necessary
 * to close it after use
 * 
 * @param <T> 
 */
public interface ScrollableResult<T> extends AutoCloseable{
    
    /**
     * Return the current row as a mapped object. The value can be cached by
     * row depending on implementation
     * @return A mapped object
     */
    T get();
    
    /**
     * Get the rowid
     * @return The row id
     */
    int getRowId();
    
    /**
     * Position the scroll in a state before the first row
     * <br>
     * This method is implementation dependent
     * 
     */
    void beforeFirst();
    
    /**
     * Position the scroll in a state after the last row
     * <br>
     * This method is implementation dependent
     * 
     */
    void afterLast();
    
    /**
     * Position the scroll at a specific row
     * <br>
     * This method is implementation dependent
     * 
     * @return true if the row was found and positioned otherwise false
     */
    boolean goToRow(int rowId);
    
    
    /**
     * Position the scroll at the previous row
     * <br>
     * This method is implementation dependent
     * 
     * @return true if the previous row exists and was found otherwise false
     */
    boolean before();
    
    /**
     * Position the scroll at the next row
     * 
     * @return true if the next row exists and was found otherwise false
     */
    boolean next();
    
}
