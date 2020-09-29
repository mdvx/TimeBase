package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class DuplicateNameException extends CompilationException {
    public DuplicateNameException (
        Expression          e,
        String              name
    )
    {
        super ("Duplicate name " + name + " in " + e, e);
    }
}
