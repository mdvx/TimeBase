package deltix.qsrv.hf.tickdb.select;

import deltix.qsrv.QSHome;
import deltix.qsrv.hf.tickdb.*;
import deltix.qsrv.hf.tickdb.comm.client.TickDBClient;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.util.concurrent.NotifyingRunnable;

import static deltix.qsrv.testsetup.TickDBCreator.*;
import org.junit.*;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

@Category(TickDBFast.class)
public class Test_NonBlockingModeChange {
    private DXTickDB                db;
    private final String            LOCATION = TDBRunner.getTemporaryLocation();

    @Before
    public final void           startup() throws Throwable {
        QSHome.set(LOCATION);
        db = openStdTicksTestDB (LOCATION);
    }

    @After
    public final void           teardown () {
        db.close ();
    }

    private class ModeSwitchTester extends TickDBTest {
        private final Runnable                      avlnr =
            new NotifyingRunnable ();            

        public ModeSwitchTester () {
        }

        @Override
        public void                 run (DXTickDB db) throws Exception {
            CursorTester    ct = new CursorTester (db, null);
            boolean         checkType = !(db instanceof TickDBClient);
            int             n = 0;
            boolean         sync = true;

            try {
                ct.reset (0, 0, 0);
                ct.setAllEntities ();
                ct.setAllTypes ();
                ct.addStreams ((1 << NUM_TEST_STREAMS) - 1);

                while (ct.checkOne (checkType, avlnr)) {
                    n++;

                    if (n % 33 == 0) {
                        sync = !sync;
                        
                        if (sync) 
                            ct.getCursor ().setAvailabilityListener (null);                        
                        else 
                            ct.getCursor ().setAvailabilityListener (avlnr);                        
                    }
                }
            } finally {
                ct.close ();
            }
        }
    }

    @Test
    public void             smokeTestLocal () throws Exception {
        new ModeSwitchTester ().run (db);
    }

    @Test
    public void             smokeTestRemote () throws Exception {
        new ModeSwitchTester ().runRemote (db);
    }   
}
