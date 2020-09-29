package deltix.qsrv.hf.tickdb.corruption;

import deltix.qsrv.QSHome;
import deltix.qsrv.hf.tickdb.TDBRunner;
import deltix.qsrv.hf.tickdb.comm.server.TestServer;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.testframework.TBServerProcess;
import deltix.util.lang.Disposable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@Category(TickDB.class)
public class Test_Corruption {

    private final static int TB_SERVER_PORT = 9887;
    private final static String TB_PROCESS_CLASS = TestTBServer.class.getName();
    private final static boolean USE_TB_RUNNER = true;
    private final static boolean SEPARATE_PROCESS = true;
    private final static int TEST_WAIT_INTERVAL = 120_000;

    public final static boolean DIRECT_LOADERS = true && !USE_TB_RUNNER;
    public final static int LOADERS_COUNT = 20;

    private static String location;
    private static TestTBServer tbServer;
    private static TBServerProcess tbServerProcess;
    private static TDBRunner runner;

    @BeforeClass
    public static void start() throws Throwable {
        System.setProperty(TickDBFactory.VERSION_PROPERTY, "5.0");

        location = TDBRunner.getTemporaryLocation("timebase");
        startTb();
    }

    @AfterClass
    public static void stop() throws Throwable {
        stopTb();
    }

    private static void startTb() throws Exception {
        if (USE_TB_RUNNER) {
            startTbRunner(location);
        } else {
            if (SEPARATE_PROCESS) {
                startTBServerProcess(location);
            } else {
                startTBServer(location);
            }
        }
    }

    private static void stopTb() throws Exception {
        if (USE_TB_RUNNER) {
            stopTbRunner();
        } else {
            if (SEPARATE_PROCESS) {
                stopTBServerProcess();
            } else {
                stopTBServer();
            }
        }
    }

    private static void startTbRunner(String workFolder) throws Exception {
        if (runner == null) {
            File tb = new File(workFolder);
            QSHome.set(tb.getParent());

            DataCacheOptions options = new DataCacheOptions(Integer.MAX_VALUE, 500 * 1024 * 1024);
            options.fs.withMaxFolderSize(10).withMaxFileSize(1024 * 1024);

            runner = new TDBRunner(true, false, tb.getAbsolutePath(), new TestServer(options, new File(workFolder)));
            runner.startup();
        } else {
            System.out.println("Runner already started");
        }
    }

    private static void stopTbRunner() throws Exception {
        if (runner != null) {
            runner.shutdown();
            runner = null;
        } else {
            System.out.println("Runner already stopped");
        }
    }

    private static void startTBServer(String workFolder) throws Exception {
        if (tbServer == null) {
            tbServer = new TestTBServer("-tb", "-home", createTbFolder(workFolder), "-port", String.valueOf(TB_SERVER_PORT));
            new Thread(() -> tbServer.start()).start();
            Thread.sleep(10000);
        } else {
            System.out.println("Timebase Server already started");
        }
    }

    private static void stopTBServer() throws IOException {
        if (tbServer != null) {
            tbServer.stop();
            tbServer = null;
        } else {
            System.out.println("Timebase Server already stopped");
        }
    }

    private static void startTBServerProcess(String workFolder) throws Exception {
        if (tbServerProcess == null) {
            tbServerProcess = new TBServerProcess(TB_PROCESS_CLASS, workFolder, createTbFolder(workFolder), TB_SERVER_PORT);
            tbServerProcess.start();

            //wait for start
            int sleeps = 0;
            while (!tbServerProcess.isStarted()) {
                Thread.sleep(1000);
                if (++sleeps > 60) {
                    break;
                }
            }
        } else {
            System.out.println("Timebase Server Process already started");
        }
    }

    private static void stopTBServerProcess() throws IOException {
        if (tbServerProcess != null) {
            tbServerProcess.stop(true);
            tbServerProcess = null;
        } else {
            System.out.println("Timebase Server Process already stopped");
        }
    }

    private static String createTbFolder(String workFolder) {
        File tbFolder = new File(workFolder, "tickdb");
        tbFolder.mkdirs();
        return tbFolder.getAbsolutePath();
    }

    private DXTickDB openTickDB() {
        if (runner != null) {
            return runner.getTickDb();
        }

        DXTickDB db = TickDBFactory.createFromUrl("dxtick://localhost:" + TB_SERVER_PORT);
        db.open(false);
        return db;
    }

    private void stopTickDB(DXTickDB db) {
        if (runner == null) {
            db.close();
        }
    }

//    @Test
    public void testMultipleLoaders() throws Exception {
        for (int i = 0 ; i < 3; ++i) {
            startTb();

            DXTickDB db = openTickDB();

            List<LoaderThread> loaders = createAndStartLoaders(db);
            doNormalizedLoading(db, 0, TEST_WAIT_INTERVAL);
            Thread.sleep(TEST_WAIT_INTERVAL);
            stopLoaders(loaders);

            stopTickDB(db);

            stopTb();
            Thread.sleep(5000);
        }
    }

    @Test
    public void testMultipleLoaders2() throws Exception {
        for (int i = 0 ; i < 3; ++i) {
            startTb();

            DXTickDB db = openTickDB();

            List<LoaderThread> loaders = createAndStartLoaders(db);
            doNormalizedLoading2(db, 0, 0, TEST_WAIT_INTERVAL);
            Thread.sleep(TEST_WAIT_INTERVAL);
            stopLoaders(loaders);

            stopTickDB(db);

            stopTb();
            Thread.sleep(5000);
        }
    }

    @Test
    public void testMultipleLoadersDetermined() throws Exception {
        for (int i = 0; i < 3; ++i) {
            startTb();
            DXTickDB db = openTickDB();

            List<DeterminedLoader> loaders = Utils.createDeterminedLoaders(
                db, LOADERS_COUNT, System.currentTimeMillis(), 5000
            );

            List<DeterminedLoaderThread> threads = loaders.stream()
                .map(l -> new DeterminedLoaderThread(l, 200 * 150))
                .collect(Collectors.toList());
            threads.forEach(Thread::start);

            doNormalizedLoading(db, i == 0 ? 12_000_000 : 18_000_000, 0);

            threads.forEach(th -> {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            stopTickDB(db);
            stopTb();
            Thread.sleep(2000);
        }
    }

//    @Test
    public void testMultipleLoaders2Determined() throws Exception {
        for (int i = 0; i < 3; ++i) {
            startTb();
            DXTickDB db = openTickDB();

            List<DeterminedLoader> loaders = Utils.createDeterminedLoaders(
                db, LOADERS_COUNT, System.currentTimeMillis(), 5000
            );

            List<DeterminedLoaderThread> threads = loaders.stream()
                .map(l -> new DeterminedLoaderThread(l, 200 * 300))
                .collect(Collectors.toList());
            threads.forEach(Thread::start);

            doNormalizedLoading2(db, 13_500_000,  755_630, 0);

            threads.forEach(th -> {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            stopTickDB(db);
            stopTb();
            Thread.sleep(2000);
        }
    }

    private List<LoaderThread> createAndStartLoaders(DXTickDB db) throws InterruptedException {
        List<LoaderThread> loaders = new ArrayList<>();
        if (!DIRECT_LOADERS) {
            loaders.addAll(Utils.createLoaders(db, LOADERS_COUNT));
        } else {
            // wait server to create streams
            Thread.sleep(1000 * LOADERS_COUNT / 2);
        }
        loaders.forEach(Thread::start);
        return loaders;
    }

    private void stopLoaders(List<LoaderThread> loaders) {
        loaders.forEach(LoaderThread::close);
        loaders.forEach(l -> {
            try {
                l.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("All loaders closed");
    }

    private void doNormalizedLoading(DXTickDB db, long copyCount, int waitInterval) throws InterruptedException {
        List<DXTickStream> streams = new ArrayList<>();
        for (int i = 0; i < LOADERS_COUNT; ++i) {
            streams.add(Utils.getOrCreateTestStream(db, i));
        }

        DXTickStream outStream = Utils.getOrCreateTestStream(db, 10000);
        outStream.truncate(0);
        try (
            TickCursor cursor = db.select(
                Long.MIN_VALUE, new SelectionOptions(false, true), streams.toArray(new DXTickStream[0])
            );
            TickLoader loader = outStream.createLoader(new LoadingOptions(false))
        ) {
            CopyThread copyThread = new CopyThread(cursor, loader, copyCount);
            copyThread.start();

            Thread.sleep(waitInterval);

            copyThread.running = false;
            copyThread.join();
        }
    }

    private void doNormalizedLoading2(DXTickDB db, long copyCount1, long copyCount2, int waitInterval) throws InterruptedException {
        List<DXTickStream> streams = new ArrayList<>();
        for (int i = 0; i < LOADERS_COUNT; ++i) {
            streams.add(Utils.getOrCreateTestStream(db, i));
        }

        DXTickStream outStream = Utils.getOrCreateTestStream(db, 10000);
        outStream.purge(System.currentTimeMillis());
        try (
            TickCursor cursor = db.select(
                System.currentTimeMillis(), new SelectionOptions(false, true), streams.toArray(new DXTickStream[0])
            );
            TickLoader loader = outStream.createLoader(new LoadingOptions(false))
        ) {
            CopyThread copyThread = new CopyThread(cursor, loader, copyCount1);
            copyThread.start();

            List<TickCursor> cursors = new ArrayList<>();
            List<TickLoader> loaders = new ArrayList<>();
            List<CopyThread> threads = new ArrayList<>();
            for (int i = 0; i < LOADERS_COUNT; ++i) {
                DXTickStream stream = Utils.getOrCreateTestStream(db, i);
                stream.purge(System.currentTimeMillis() - 1000);

                TickCursor c = db.select(
                    System.currentTimeMillis(), new SelectionOptions(false, true), stream
                );
                cursors.add(c);

                TickLoader l = Utils.getOrCreateTestStream(db, 1000 + i).createLoader(new LoadingOptions(false));
                loaders.add(l);

                threads.add(new CopyThread(c, l, copyCount2));
            }

            threads.forEach(Thread::start);

            Thread.sleep(waitInterval);

            copyThread.running = false;
            copyThread.join();

            threads.forEach(t -> t.running = false);
            threads.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            cursors.forEach(Disposable::close);
            loaders.forEach(Disposable::close);
        }
    }

    private static class CopyThread extends Thread {

        private final TickCursor cursor;
        private final TickLoader loader;
        private volatile boolean running = true;
        private long copyCount;
        private long totalLoad = 0;

        private CopyThread(TickCursor cursor, TickLoader loader, long count) {
            this.cursor = cursor;
            this.loader = loader;
            this.copyCount = count;
        }

        @Override
        public void run() {
            if (copyCount > 0) {
                for (int i = 0; i < copyCount; ++i) {
                    cursor.next();
                    loader.send(cursor.getMessage());
                    ++totalLoad;
                }
            } else {
                while (cursor.next() && running) {
                    loader.send(cursor.getMessage());
                    ++totalLoad;
                }
            }

            System.out.println("Total copy: " + totalLoad);
        }
    }

    private static class DeterminedLoaderThread extends Thread {
        private final DeterminedLoader loader;
        private long count;

        public DeterminedLoaderThread(DeterminedLoader loader, long count) {
            this.loader = loader;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < count; ++i) {
                    loader.load(5);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                loader.close();
            }

        }
    }
}
