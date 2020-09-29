package deltix.qsrv.hf.tickdb;

import deltix.qsrv.hf.tickdb.comm.client.TickDBClient;
import deltix.qsrv.hf.tickdb.comm.server.TickDBServer;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.util.lang.Util;

/**
 *  Encapsulates the logic for starting a server.
 */
public abstract class TickDBTest {
    private boolean                 isRemote;

    public abstract void            run (DXTickDB db) throws Exception;

    protected final boolean         isRemote () {
        return (isRemote);
    }

    public final void               runRemote (DXTickDB localDB)
        throws Exception
    {
        TickDBServer            server = new TickDBServer (0, localDB);

        DXTickDB                conn = null;

        try {
            server.start ();

            conn = new TickDBClient ("localhost", server.getPort ());

            conn.open (localDB.isReadOnly ());

            if (!Boolean.getBoolean ("quiet"))
                System.out.println ("Connected to " + conn.getId ());

            isRemote = true;
            run (conn);

            conn.close ();
            conn = null;
        } finally {
            isRemote = false;
            Util.close (conn);
            server.shutdown (true);
        }
    }
}
