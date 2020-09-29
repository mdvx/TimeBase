package deltix.snmp.mibc.errors;

import deltix.snmp.parser.*;
import deltix.util.parsers.CompilationException;

/**
 *
 */
public class IllegalTypeException extends CompilationException {
    public IllegalTypeException (Type type) {
        super ("This type is not supported by SMIv2", type.location);        
    }            
}
