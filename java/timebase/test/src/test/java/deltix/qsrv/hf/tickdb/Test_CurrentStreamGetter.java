package deltix.qsrv.hf.tickdb;


import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.pub.*;

import deltix.util.lang.Util;
import org.junit.*;
import static org.junit.Assert.*;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

/**
 *  Test transient stream features.
 */
@Category(TickDBFast.class)
public class Test_CurrentStreamGetter {
    private static final long       T = 1262106445000L;
    private static final int        NS = 10;
    private static final int        NM = 100;

    private DXTickDB                db;

    @Before
    public final void           startup() throws Throwable {
        RecordClassDescriptor       rcd =
            StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ();

        deltix.timebase.api.messages.TradeMessage msg = new deltix.timebase.api.messages.TradeMessage();

        msg.setSymbol("DLTX");

        db = TickDBFactory.create (TDBRunner.getTemporaryLocation());

        db.format ();

        //  Create NS streams
        for (int ii = 0; ii < NS; ii++) {
            long                tt = T + ii;
            StreamOptions       options =
                new StreamOptions (StreamScope.DURABLE, "Stream #" + ii, null, 1);

            options.setFixedType (rcd);

            DXTickStream        stream = db.createStream ("S" + ii, options);            
            TickLoader          loader = stream.createLoader ();

            //  Load NM messages starting at T + ii, one per second.
            for (int jj = 0; jj < NM; jj++) {
                msg.setTimeStampMs(tt + jj * 1000);
                msg.setSize(ii);
                msg.setPrice(jj);
                
                loader.send (msg);
            }

            loader.close ();
        }

        db.close ();

        db.open (true);
    }

    @After
    public final void           teardown () {
        db.close ();
    }

    class CurrentStreamGetterTester extends TickDBTest {
        DXTickStream []             streams = new DXTickStream [NS];
        TickCursor                  cursor;

        public CurrentStreamGetterTester () {
        }

        private void                check (
            int                         sidx,
            int                         midx
        )
        {
            assertTrue (cursor.next ());

            deltix.timebase.api.messages.TradeMessage m = (deltix.timebase.api.messages.TradeMessage) cursor.getMessage ();

            assertNotNull (m);

            assertEquals (sidx, (int) m.getSize());
            assertEquals (midx, (int) m.getPrice());
            assertEquals (T + midx * 1000 + sidx, m.getTimeStampMs());

            int                         csidx = cursor.getCurrentStreamIndex ();

            assertTrue (csidx >= 0);

            TickStream                  s = streams [sidx];

            assertEquals (s.getKey (), cursor.getCurrentStreamKey ());
            assertEquals (s, cursor.getCurrentStream ());
        }

        @Override
        public void                 run (DXTickDB db) throws Exception {
            SelectionOptions            options = new SelectionOptions ();
            //
            //  Without this, the remote test will fail beacuse of the client
            //  optimization (output locally stored messages).
            //
            for (int ii = 0; ii < NS; ii++)
                streams [ii] = db.getStream ("S" + ii);

            try {
                cursor = db.createCursor (options, streams [0], streams [3], streams [9]);

                cursor.subscribeToAllEntities();
                cursor.reset (T);

                check (0, 0);
                check (3, 0);
                check (9, 0);
                check (0, 1);
                check (3, 1);
                check (9, 1);

                cursor.addStream (streams [7]);

                check (0, 2);
                check (3, 2);
                check (7, 2);
                check (9, 2);

                cursor.removeStream (streams [0]);

                check (3, 3);
                check (7, 3);
                check (9, 3);

                cursor.removeAllStreams ();

                //Commented out due to BUG 6433
                assertFalse (cursor.next ());

                cursor.close ();
            } finally {
                Util.close(cursor);
            }
        }
    }

    @Test
    public void             getterTestLocal () throws Exception {
        new CurrentStreamGetterTester ().run (db);
    }

    @Test
    public void             getterTestRemote () throws Exception {
        new CurrentStreamGetterTester ().runRemote (db);
    }
    

}
