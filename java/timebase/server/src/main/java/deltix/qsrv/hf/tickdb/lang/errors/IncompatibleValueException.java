package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;

public class IncompatibleValueException extends CompilationException {

    public IncompatibleValueException(String msg, long location) {
        super(msg, location);
    }
}
