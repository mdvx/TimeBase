package deltix.snmp.mibc;

import deltix.snmp.parser.*;
import deltix.snmp.smi.SMIType;

/**
 *
 */
class CompiledTypeBean 
    extends CompiledEntityBean <TypeDefinition> 
    implements CompiledType 
{
    SMIType                 type = null;
    
    CompiledTypeBean (TypeDefinition def) {
        super (def);
    }

    @Override
    public SMIType          getType () {
        if (type == null)
            throw new IllegalStateException ("uncompiled");
        
        return (type);
    }        
}
