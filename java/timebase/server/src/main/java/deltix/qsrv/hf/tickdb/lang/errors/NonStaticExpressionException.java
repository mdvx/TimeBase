package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.Expression;

/**
 *
 */
public class NonStaticExpressionException extends CompilationException {
    public NonStaticExpressionException (Expression e) {
        super (
            "The value of " + e + " cannot be computed.",
            e
        );
    }
}
