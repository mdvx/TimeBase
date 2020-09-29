package deltix.qsrv.hf.tickdb.impl;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.TickStream;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;
import deltix.qsrv.hf.tickdb.pub.query.Parameter;
import deltix.qsrv.hf.tickdb.pub.query.PreparedQuery;

/**
 *
 */
public interface PQExecutor {

    InstrumentMessageSource executePreparedQuery (
            PreparedQuery           pq,
            SelectionOptions        options,
            TickStream[]            streams,
            CharSequence[]          ids,
            boolean                 fullScan,
            long                    time,
            Parameter[]             params);
}
