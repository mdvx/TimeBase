package deltix.qsrv.hf.tickdb.pub.query;

import deltix.qsrv.hf.pub.ReadableValue;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.util.lang.Disposable;

/**
 *  Analogous to JDBC's PreparedStatement
 */
public interface PreparedQuery extends Disposable {
    public boolean                      isReverse ();
    
    public InstrumentMessageSource      executeQuery (
        SelectionOptions                    options,
        ReadableValue []                    params
    );
}
