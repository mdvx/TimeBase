package deltix.snmp.script;

import deltix.snmp.mtree.MOPrimitive;

/**
 * 
 */
public final class SSPrimitive extends SSNode <MOPrimitive> {
    public SSPrimitive (
        SSRoot                  root,
        MOPrimitive             mo
    ) 
    {
        super (root, mo);                
    }

    @Override
    public Object       getDefaultValue (Class type) {
        return (mo.getValue ());
    }                
}
