package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.lang.pub.DataTypeSpec;
import deltix.util.parsers.CompilationException;

/**
 *
 */
public class IllegalAbstractDataType extends CompilationException {
    public IllegalAbstractDataType(DataTypeSpec dts, RecordClassDescriptor descriptor) {
        super("Illegal abstract type: " + descriptor + ".", dts);
    }
}
