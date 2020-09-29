package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.TypeIdentifier;

/**
 *
 */
public class IllegalSupertypeException extends CompilationException {
    public IllegalSupertypeException (TypeIdentifier id) {
        super ("Illegal supertype " + id, id);
    }
}
