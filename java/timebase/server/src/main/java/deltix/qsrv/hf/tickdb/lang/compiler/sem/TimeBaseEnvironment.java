package deltix.qsrv.hf.tickdb.lang.compiler.sem;

import deltix.qsrv.hf.tickdb.lang.pub.NamedObjectType;
import deltix.qsrv.hf.tickdb.pub.*;

/**
 *
 */
public class TimeBaseEnvironment extends EnvironmentFrame {
    public TimeBaseEnvironment (TickDB db) {
        this (db, null);
    }
     
    public TimeBaseEnvironment (TickDB db, Environment parent) {
        super (parent);
        
        for (TickStream s : db.listStreams ())
            bind (NamedObjectType.VARIABLE, s.getKey (), s);                   
    }
}
