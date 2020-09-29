package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class WrongNumArgsException extends CompilationException {
    public WrongNumArgsException (CallExpression e, int num) {
        super ("Function " + e.name + " () may not be applied to " + num + " arguments.", e);
    }
}
