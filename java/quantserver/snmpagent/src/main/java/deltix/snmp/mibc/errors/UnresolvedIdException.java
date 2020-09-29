package deltix.snmp.mibc.errors;

import deltix.util.parsers.CompilationException;

/**
 *
 */
public class UnresolvedIdException extends CompilationException {
    public final String             id;

    public UnresolvedIdException (long location, String id) {
        super ("Unresolved object identifier: " + id, location);
        
        this.id = id;
    }
    
    
    
}
