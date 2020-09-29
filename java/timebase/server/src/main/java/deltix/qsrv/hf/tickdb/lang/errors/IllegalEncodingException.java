package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *  Encoding is incompatible with type.
 */
public class IllegalEncodingException extends CompilationException {
    public IllegalEncodingException (SimpleDataTypeSpec dts) {
        super (
            "Encoding " + dts.encoding + " is illegal in type " + dts.typeId, 
            dts
        );
    }    
}
