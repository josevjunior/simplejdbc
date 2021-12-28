
package io.github.josevjunior.simplejdbc;

/**
 * A wrapper that provides a way to navigate through resultset without 
 * necessarily map all the records before that. The advanced method navigation
 * are jdbc driver dependent
 * @param <T> 
 */
public interface ScrollableResult<T> extends AutoCloseable{
    
    T get();
    
    int getRowId();
    
    boolean goToFirst();
    
    boolean goToLast();
    
    boolean goToRow(int rowId);
    
    boolean before();
    
    boolean next();
    
    
    
    
    
}
