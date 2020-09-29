package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.util.parsers.Element;

public class ValueUndefinedException extends CompilationException {
    public ValueUndefinedException(String msg, long location) {
        super(msg, location);
    }

    public ValueUndefinedException(String msg, Element[] elems) {
        super(msg, elems);
    }

    public ValueUndefinedException(String msg, Element elem) {
        super(msg, elem);
    }
}
