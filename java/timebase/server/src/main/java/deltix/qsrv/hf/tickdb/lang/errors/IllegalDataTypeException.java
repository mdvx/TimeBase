package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.qsrv.hf.tickdb.lang.pub.DataTypeSpec;
import deltix.util.parsers.CompilationException;

import java.util.Arrays;

/**
 *
 */
public class IllegalDataTypeException extends CompilationException {
    public IllegalDataTypeException(DataTypeSpec dts, Class<?> actual, Class<?>... expected) {
        super("Unexpected type of object. \n" +
              "Expected: " + Arrays.toString(expected) + "; Actual: " + actual + ".",
              dts);
    }
}
