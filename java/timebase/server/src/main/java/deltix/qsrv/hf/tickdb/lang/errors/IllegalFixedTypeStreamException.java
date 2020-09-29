package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class IllegalFixedTypeStreamException extends CompilationException {
    public IllegalFixedTypeStreamException (
        OptionElement               option, 
        int                         numConcreteClasses
    )
    {
        super (
            "A fixedType stream must have exactly one non-instantiable class "
                + "defined. Found " + numConcreteClasses, 
            option
        );
    }        
}
