package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class IllegalDefaultValueException extends CompilationException {
    public IllegalDefaultValueException (Expression x) {
        super ("Default value specification is not allowed here.", x);
    }    
}
