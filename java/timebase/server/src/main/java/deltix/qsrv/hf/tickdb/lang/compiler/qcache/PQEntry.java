package deltix.qsrv.hf.tickdb.lang.compiler.qcache;

import deltix.qsrv.hf.tickdb.pub.query.PreparedQuery;
import deltix.util.collections.QuickList;
import deltix.util.time.TimeKeeper;

/**
 *
 */
class PQEntry extends QuickList.Entry <PQEntry> {
    final PQKey                         key;
    final PreparedQuery                 query;
    long                                timestamp;
    
    PQEntry (PQKey key, PreparedQuery query) {
        this.key = key;
        this.query = query;
        this.timestamp = TimeKeeper.currentTime;
    }        
}
