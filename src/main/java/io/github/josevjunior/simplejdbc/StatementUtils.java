
package io.github.josevjunior.simplejdbc;

public class StatementUtils {
    
    public static final Object EMPTY_OBJECT = new Object();
    
    public static boolean isStringOrNumber(Object n) {
        return (n instanceof String || n instanceof Integer);
    }
    
}
