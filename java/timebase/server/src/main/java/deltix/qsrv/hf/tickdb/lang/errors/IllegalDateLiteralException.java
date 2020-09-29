package deltix.qsrv.hf.tickdb.lang.errors;

import deltix.util.parsers.CompilationException;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *
 */
public class IllegalDateLiteralException extends CompilationException {
    public enum Field {
        FORMAT,
        YEAR,
        MONTH,
        DAY,
        HOUR,
        MINUTE,
        SECOND,
        FRACTION,
        TIMEZONE
    }
    
    public final Field      field;
    
    public IllegalDateLiteralException (
        long                location,
        String              text,
        Field               field
    )
    {
        super ("Illegal " + field + " in date literal '" + text + "'", location);
        
        this.field = field;
    }
}
