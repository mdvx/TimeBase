package deltix.snmp.mtree;

import deltix.snmp.smi.*;

/**
 * 
 */
public final class MOCategory extends MOContainer {    
    MOCategory (
        MONode <?>              parent, 
        SMIPrimitiveContainer   prototype
    )
    {
        super (parent, prototype);
        
        setUpScalarChildren ();
    }   
}
