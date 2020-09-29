package deltix.qsrv.hf.tickdb.lang.compiler.sem;

import deltix.qsrv.hf.tickdb.lang.pub.NamedObjectType;

/**
 *  Interface for looking up objects by type and name.
 */
public interface Environment {
    public Object           lookUp (NamedObjectType type, String id, long location);
}
