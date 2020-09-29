package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.pub.md.ClassDataType;
import deltix.qsrv.hf.tickdb.lang.pub.Expression;

/**
 *
 */
public class PolymorphicTypeException extends CompilationException {
    public PolymorphicTypeException (Expression e, ClassDataType actual) {
        super (
            "Illegal type in: " + e +
            "; expected FIXED type; found POLYMORPHIC: " + actual,
            e
        );
    }
}
