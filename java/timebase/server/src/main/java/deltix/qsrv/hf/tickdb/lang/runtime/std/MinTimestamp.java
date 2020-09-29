package deltix.qsrv.hf.tickdb.lang.runtime.std;

import deltix.qsrv.hf.pub.md.DateTimeDataType;
import deltix.qsrv.hf.tickdb.lang.pub.*;

/**
 *  Aggregate MIN (TIMESTAMP)
 */
@Aggregate @FunctionInfo (id = "MIN", returns = "TIMESTAMP?", args = { "TIMESTAMP?" })
public final class MinTimestamp {
    private long        minValue = DateTimeDataType.NULL;

    public long         get () {
        return (minValue);
    }

    public void         set1 (long v) {
        if (v != DateTimeDataType.NULL &&
            (minValue == DateTimeDataType.NULL || v < minValue))
            minValue = v;
    }        
}
