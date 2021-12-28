
package io.github.josevjunior.simplejdbc;

public class JdbcException extends RuntimeException {

    public JdbcException() {
    }

    public JdbcException(String string) {
        super(string);
    }

    public JdbcException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public JdbcException(Throwable thrwbl) {
        super(thrwbl);
    }

    
    
    
    
    
}
