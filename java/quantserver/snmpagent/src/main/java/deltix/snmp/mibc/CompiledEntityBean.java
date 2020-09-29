package deltix.snmp.mibc;

import deltix.snmp.parser.*;

/**
 *
 */
abstract class CompiledEntityBean <T extends Definition <?>> 
    implements CompiledEntity 
{
    public enum RefState {
        INIT,
        RESOLVING,
        RESOLVED
    }
    
    protected final T           def;
    RefState                    state = RefState.INIT;
    
    CompiledEntityBean (T def) {
        this.def = def;
    }
                            
    @Override
    public String               getId () {
        return (def.id);
    }        
}
