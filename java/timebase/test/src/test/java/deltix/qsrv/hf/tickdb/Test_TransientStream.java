package deltix.qsrv.hf.tickdb;

import deltix.timebase.messages.IdentityKey;

import deltix.qsrv.hf.tickdb.pub.*;

import deltix.util.time.TimeKeeper;
import deltix.util.lang.Util;
import org.junit.*;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

/**
 *  Test transient stream features.
 */
@Category(TickDBFast.class)
public class Test_TransientStream {
    public static final String      STREAM_KEY = "test.stream";

    private DXTickDB     db;

    @Before
    public final void           startup() throws Throwable {
        db = TickDBFactory.create (TDBRunner.getTemporaryLocation());

        db.format ();
    }

    @After
    public final void           teardown () {
        db.close ();
    }

    @Test
    public void testSelect() throws Exception {
        StreamOptions               options =
            new StreamOptions (
                StreamScope.TRANSIENT,
                "Stream Name",
                "Description Line1\nLine 2\nLine 3",
                1
            );

        options.setFixedType (StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ());
        (options.bufferOptions = new BufferOptions()).lossless = true;

        final DXTickStream      stream = db.createStream (STREAM_KEY, options);
        TickCursor cursor = stream.select(Long.MAX_VALUE, null, new String[0], new IdentityKey[0]);

        cursor.subscribeToAllEntities();
        cursor.subscribeToAllTypes();

        assertTrue(!cursor.next());
    }

    class TransientStreamTester extends TickDBTest {
        public TransientStreamTester () {
        }

        @Override
        public void                 run (DXTickDB db) throws Exception {
            StreamOptions               options =
                new StreamOptions (
                    StreamScope.TRANSIENT,
                    "Stream Name",
                    "Description Line1\nLine 2\nLine 3",
                    1
                );

            options.setFixedType (StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ());

            final DXTickStream      stream = db.createStream (STREAM_KEY, options);

            deltix.timebase.api.messages.TradeMessage msg = new deltix.timebase.api.messages.TradeMessage();


            msg.setSymbol("DLTX");

            TickLoader              loader = stream.createLoader ();

            msg.setTimeStampMs(TimeKeeper.currentTime);
            loader.send (msg);

            loader.close ();

            long []                 tr = stream.getTimeRange ();
        }
    }

     class TransientStreamTester1 extends TickDBTest {
        public TransientStreamTester1 () {
        }

        @Override
        public void                 run (DXTickDB db) throws Exception {
            StreamOptions               options =
                new StreamOptions (
                    StreamScope.TRANSIENT,
                    "Stream Name",
                    "Description Line1\nLine 2\nLine 3",
                    1
                );

            options.setFixedType (StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ());

            final DXTickStream      stream = db.createStream (STREAM_KEY, options);

            TDBRunner.TradesGenerator generator =
                    new TDBRunner.TradesGenerator(
                        new GregorianCalendar(2009, 1, 1),
                            (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, -1, "DLTX", "ORCL");

            boolean passed = false;
            TickLoader        loader = null;
            try {
                loader = stream.createLoader ();
                int count = 0;
                while (generator.next()) {
                    loader.send(generator.getMessage());
                    count++;
                    if (count == 1000)
                        stream.delete();
                }
                loader.close();
                loader = null;
            }
            catch (WriterAbortedException e) {
                // valid case
                passed = true;
            }
            finally {
                Util.close(loader);
            }
            
            assertTrue(passed);
        }
    }

    @Test (timeout=60000)
    public void             transStreamTestLocal () throws Exception {
        new TransientStreamTester ().run (db);
    }

    @Test (timeout=60000)
    public void             transStreamTestRemote () throws Exception {
        new TransientStreamTester ().runRemote (db);
    }

    @Test (timeout=60000)
    public void             testDelete() throws Exception {
        new TransientStreamTester1 ().runRemote (db);
    }    
}
