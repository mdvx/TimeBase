package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.Null;

/**
 *
 */
public class UnacceptableNullException extends CompilationException {
    public UnacceptableNullException (Null e) {
        super (
            "null is not allowed here",
            e
        );
    }
}
