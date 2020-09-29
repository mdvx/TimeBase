package deltix.qsrv.hf.tickdb.lang.compiler.sem;

import deltix.qsrv.hf.pub.md.DataField;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.util.lang.Util;

/**
 *
 */
public class DataFieldRef {
    public final RecordClassDescriptor       parent;
    public final DataField                   field;

    public DataFieldRef (RecordClassDescriptor parent, DataField field) {
        this.parent = parent;
        this.field = field;
    }

    @Override
    public boolean          equals (Object obj) {
        if (this == obj)
            return (true);

        if (!(obj instanceof DataFieldRef))
            return (false);

        final DataFieldRef      other = (DataFieldRef) obj;

        return (parent.equals (other.parent) && Util.xequals (field.getName (), other.field.getName ()));
    }

    @Override
    public int              hashCode () {
        return (parent.hashCode () + Util.hashCode (field.getName ()));
    }
}
