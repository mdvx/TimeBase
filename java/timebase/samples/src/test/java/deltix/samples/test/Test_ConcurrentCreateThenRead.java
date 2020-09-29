package deltix.samples.test;

import deltix.qsrv.hf.pub.md.Introspector;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.comm.client.ServerException;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.StreamOptions;
import deltix.qsrv.hf.tickdb.pub.StreamScope;
import deltix.qsrv.hf.tickdb.pub.TickCursor;
import deltix.samples.timebase.BarMessage;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs 3 threads.
 * Two treads attempt to create a stream and then read a message from it.
 * Third thread polls TB for this stream and then tries to read message.
 * <p>
 * Time must guarantee that:
 * <ul>
 *     <li>
 *         Exactly one thread successfully creates stream.
 *     </li>
 *     <li>
 *         Thread that failed to create stream must get "Duplicate stream key" message.
 *     </li>
 *     <li>
 *         Polling thread must successfully open cursor as soon as stream created.
 *         Client may not observer partially created streams.
 *     </li>
 * </ul>
 */
public class Test_ConcurrentCreateThenRead extends TDBTestBase {

    static RecordClassDescriptor BAR_DESCRIPTOR;

    static {
        try {
            BAR_DESCRIPTOR = (RecordClassDescriptor) Introspector.introspectSingleClass(BarMessage.class);
        } catch (Introspector.IntrospectionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runTest() throws InterruptedException {
        DXTickDB db = getTickDb();

        for (int i = 0; i < 20; i++) {
            testIteration(db);
        }
    }

    private void testIteration(DXTickDB db) throws InterruptedException {
        CreateStreamTask t1 = new CreateStreamTask(db);
        CreateStreamTask t2 = new CreateStreamTask(db);
        PollStreamTask t3 = new PollStreamTask(db);

        Thread[] executors = new Thread[50];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = new Thread(i % 3 == 0 ? t1 : (i % 3 == 1 ? t2 : t3));
        }

        for (int i = 0; i < executors.length; i++) {
            executors[i].start();
        }

        for (int i = 0; i < executors.length; i++) {
            executors[i].join();
        }


        if (!t1.success.get() || !t2.success.get() || !t3.success.get()) {
            throw new AssertionError("One of threads failed");
        }

        db.getStream("test").delete();
    }


    private static class CreateStreamTask implements Runnable {

        private final DXTickDB db;
        private final AtomicBoolean success = new AtomicBoolean(false);

        public CreateStreamTask(DXTickDB db) {
            this.db = db;
        }

        @Override
        public void run() {
            String name = "test";
            StreamOptions streamOptions =
                    StreamOptions.fixedType(StreamScope.DURABLE, name, name, 0, BAR_DESCRIPTOR);

            try {
                DXTickStream stream;
                try {
                    stream = db.createStream("test", streamOptions);
                } catch (ServerException | IllegalArgumentException e) {
                    if (!e.getMessage().endsWith("Duplicate stream key: test")) {
                        throw new AssertionError("Unexpected message", e);
                    }
                    stream = db.getStream("test");
                }

                SelectionOptions options = new SelectionOptions();
                TickCursor cursor = stream.createCursor(options);
                cursor.reset(Long.MIN_VALUE);
                cursor.next();
                cursor.close();

                success.set(true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    private static class PollStreamTask implements Runnable {

        private final DXTickDB db;
        private final AtomicBoolean success = new AtomicBoolean(false);

        public PollStreamTask(DXTickDB db) {
            this.db = db;
        }

        @Override
        public void run() {
            try {
                DXTickStream stream = null;
                while (stream == null) {
                    stream = db.getStream("test");
                }

                SelectionOptions options = new SelectionOptions();
                TickCursor cursor = stream.createCursor(options);
                cursor.reset(Long.MIN_VALUE);
                cursor.next();
                cursor.close();

                success.set(true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}