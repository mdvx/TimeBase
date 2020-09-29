package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.pub.md.DataType;
import deltix.qsrv.hf.tickdb.lang.pub.Expression;

/**
 *
 */
public class ClassTypeExpectedException extends CompilationException {
    public ClassTypeExpectedException (Expression e, DataType actual) {
        super (
            "Illegal type in: " + e +
            "; expected: CLASS; found: " + actual.getBaseName (),
            e
        );
    }
}
