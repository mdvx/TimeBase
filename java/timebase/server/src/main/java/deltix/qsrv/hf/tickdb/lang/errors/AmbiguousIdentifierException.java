package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;

/**
 *
 */
public class AmbiguousIdentifierException extends CompilationException {
    public AmbiguousIdentifierException (String id, long location) {
        super ("Ambiguous identifier: " + id, location);
    }
}
