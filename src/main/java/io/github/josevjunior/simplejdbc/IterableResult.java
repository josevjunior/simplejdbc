
package io.github.josevjunior.simplejdbc;

public interface IterableResult<T> extends Iterable<T>{
    
    boolean last();
    
    boolean beforeFirst();
    
    long getRowId();
    
    void absolute(long rowId);
    
    boolean previous();
    
}
