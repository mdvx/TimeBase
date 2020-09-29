package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class UnknownIdentifierException extends CompilationException {
    public UnknownIdentifierException (Identifier id) {
        super ("Unknown identifier: " + id, id);
    }
    
    public UnknownIdentifierException (TypeIdentifier id) {
        super ("Unknown type: " + id, id);
    }
    
    public UnknownIdentifierException (String id, long location) {
        super ("Unknown identifier: " + id, location);
    }
}
