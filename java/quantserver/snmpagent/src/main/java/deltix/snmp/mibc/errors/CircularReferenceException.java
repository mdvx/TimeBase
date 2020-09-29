package deltix.snmp.mibc.errors;

import deltix.snmp.parser.Definition;
import deltix.util.parsers.CompilationException;

/**
 *
 */
public class CircularReferenceException extends CompilationException {
    public final String             id;

    public CircularReferenceException (Definition <?> def) {
        super ("Descriptor " + def.id + " is involved in a circular dependency", def.location);
        
        this.id = def.id;
    }            
}
