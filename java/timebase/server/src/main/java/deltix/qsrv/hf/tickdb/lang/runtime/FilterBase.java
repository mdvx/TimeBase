package deltix.qsrv.hf.tickdb.lang.runtime;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.query.*;

/**
 *  Base class for compiled filters.
 */
public abstract class FilterBase implements PreparedQuery {
    public PreparedQuery                sourceQuery;
    public RecordClassDescriptor []     types;
    public RecordClassDescriptor []     inputTypes;
    public RecordClassDescriptor []     outputTypes;

    public boolean                      isReverse () {
        return (sourceQuery.isReverse ());
    }
    
    public void                         close () {
        sourceQuery.close ();
        sourceQuery = null;
        types = null;
    }
}
