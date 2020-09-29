package deltix.qsrv.hf.tickdb.replication;

import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;

/**
 *
 */
public class StreamStorage implements Storage {
    public DXTickDB     db; // source database
    public String       name; // source stream

    public StreamStorage(DXTickDB db, String stream) {
        this.db = db;
        this.name = stream;
    }

    public DXTickStream getSource() {
        return db.getStream(name);
    }
}
