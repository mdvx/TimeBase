package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.Expression;

/**
 *
 */
public class IllegalStreamSelectorException extends CompilationException {
    public IllegalStreamSelectorException (Expression e) {
        super ("Selection mode modifier must be applied directly to a stream.", e);
    }
}
