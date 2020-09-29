package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;

public class ConstraintViolationException extends CompilationException {

    public ConstraintViolationException(String msg, long location) {
        super(msg, location);
    }
}
