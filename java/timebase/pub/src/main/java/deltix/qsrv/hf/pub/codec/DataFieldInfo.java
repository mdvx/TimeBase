package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.pub.md.*;

/**
 *
 */
public interface DataFieldInfo {
    public RecordClassDescriptor    getOwner ();

    public String                   getName ();

    public String                   getTitle ();
    
    public DataType                 getType ();
}
