package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;

/**
 *
 */
public class EmptyProgramException extends CompilationException {
    public EmptyProgramException () {
        super ("Empty", 0);
    }
}
