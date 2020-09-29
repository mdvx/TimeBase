package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class DuplicateIdentifierException extends CompilationException {
    public DuplicateIdentifierException (String id, long location) {
        super ("Duplicate object: " + id, location);
    }
    
    public DuplicateIdentifierException (Identifier id) {
        super ("Duplicate object: " + id.id, id);
    }
    
    public DuplicateIdentifierException (TypeIdentifier id) {
        super ("Duplicate type: " + id.typeName, id);
    }
}
