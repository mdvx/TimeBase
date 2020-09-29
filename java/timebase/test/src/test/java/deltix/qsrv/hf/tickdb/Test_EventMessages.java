package deltix.qsrv.hf.tickdb;

import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.pub.util.LiveCursorWatcher;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.lock.DBLock;
import deltix.qsrv.hf.tickdb.pub.lock.LockType;
import deltix.util.lang.Util;
import org.junit.Test;

import java.util.GregorianCalendar;

import static junit.framework.Assert.assertEquals;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

@Category(TickDBFast.class)
public class Test_EventMessages extends TDBRunnerBase {

    public DXTickStream getStream(DXTickDB db, String name) {
        return getStream(db, name, 0);
    }

    public DXTickStream getStream(DXTickDB db, String name, int df) {
        DXTickStream stream = db.getStream(name);
        if (stream != null)
            stream.delete();

        RecordClassDescriptor rcd = StreamConfigurationHelper.mkUniversalBarMessageDescriptor();

        StreamOptions options = StreamOptions.fixedType(StreamScope.DURABLE, name, name, df, rcd);
        options.bufferOptions = new BufferOptions ();
        return db.createStream(name, options);
    }

    @Test
    public void test1() throws InterruptedException {

        DXTickDB tickDb = getTickDb();
        //DXTickDB tickDb = new TickDBClient("localhost", 8011); // runner.getTickDb();
        //tickDb.open(false);

        DXTickStream stream = getStream(tickDb, "events");

        TDBRunner.BarsGenerator gn = new TDBRunner.BarsGenerator(
                new GregorianCalendar(2009, 1, 1), (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 10000,
                "AAPL", "MSFT");

        final TickCursor cursor = tickDb.getStream(TickDBFactory.EVENTS_STREAM_NAME).select(0,
                new SelectionOptions(false, true));

        final int count[] = new int[1];
        LiveCursorWatcher watcher = new LiveCursorWatcher(cursor, new LiveCursorWatcher.MessageListener() {
            @Override
            public void onMessage(InstrumentMessage m) {
                count[0]++;
                if (count[0] == 2000)
                    cursor.close();
                //System.out.println(cursor.getMessage());
            }
        });

        for (int i = 0; i < 1000; i++) {
            DBLock lock = stream.lock();
            TickLoader loader = stream.createLoader();

            try {
                 while (gn.next()) {
                    loader.send(gn.getMessage());
                 }
            } finally {
                Util.close(loader);
            }

            lock.release();
        }

        watcher.join();

        assertEquals(2000, count[0]);

        stream.delete();
    }

    @Test
    public void test2() throws InterruptedException {

        TDBRunner.BarsGenerator gn = new TDBRunner.BarsGenerator(
                new GregorianCalendar(2009, 1, 1), (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 10000,
                "AAPL", "MSFT");

        DXTickDB client = getTickDb();
        
        DXTickStream stream = getStream(client, "events1");

        final TickCursor cursor = client.getStream(TickDBFactory.EVENTS_STREAM_NAME).select(
                TimeConstants.USE_CURRENT_TIME, new SelectionOptions(false, true));

        final int count[] = new int[1];
        LiveCursorWatcher watcher = new LiveCursorWatcher(cursor, new LiveCursorWatcher.MessageListener() {
            @Override
            public void onMessage(InstrumentMessage m) {
                count[0]++;
                if (count[0] == 4)
                    cursor.close();
                //System.out.println(cursor.getMessage());
            }
        });

        DBLock lock = stream.lock();
        TickLoader loader = stream.createLoader ();
        try {
             while (gn.next()) {
                loader.send(gn.getMessage());
             }
        } finally {
            Util.close(loader);
        }

        lock.release();

        lock = stream.lock(LockType.READ);
        lock.release();

        watcher.join();

        assertEquals(4, count[0]);
    }
}
