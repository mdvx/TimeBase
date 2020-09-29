package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.Expression;

/**
 *
 */
public class IllegalMessageSourceException extends CompilationException {
    public IllegalMessageSourceException (Expression e) {
        super ("Illegal message source: " + e, e);
    }
}
