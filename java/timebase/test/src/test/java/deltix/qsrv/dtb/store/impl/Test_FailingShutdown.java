package deltix.qsrv.dtb.store.impl;

import deltix.gflog.Log;
import deltix.gflog.LogFactory;
import deltix.qsrv.dtb.StreamTestHelpers;
import deltix.qsrv.dtb.fs.local.FailingFileSystem;
import deltix.qsrv.dtb.fs.local.FailingPathImpl;
import deltix.qsrv.dtb.fs.local.LocalFS;
import deltix.qsrv.dtb.fs.pub.FSFactory;
import deltix.qsrv.hf.tickdb.impl.TickDBImpl;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.util.io.GUID;
import deltix.util.io.Home;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Alexei Osipov
 */
public class Test_FailingShutdown {
    private static final Log LOG = LogFactory.getLog(FailingPathImpl.class);

    @Test
    public void ioExceptionOnSave() throws InterruptedException {
        String temporaryLocation = getTemporaryLocation();
        LOG.info("Temp DB location: " + temporaryLocation);
        File folder = new File(temporaryLocation);

        LocalFS localFs = new LocalFS();

        FailingFileSystem failingFs = new FailingFileSystem(localFs);

        FSFactory.forceSetLocalWrappedFS(failingFs);

        TickDBImpl tickDB = new TickDBImpl(folder);


        failingFs.failOnFileName("\\tickdb\\test1\\data\\tmp.z0001.dat", () -> new IOException("TEST: File not found"));

        tickDB.open(false);
        try {

            DXTickStream stream1 = StreamTestHelpers.createTestStream(tickDB, "test1");
            DXTickStream stream2 = StreamTestHelpers.createTestStream(tickDB, "test2");

            StreamTestHelpers.MessageGenerator loader1 = StreamTestHelpers.createDefaultLoaderRunnable(stream1);
            Thread loaderThread1 = new Thread(loader1);

            StreamTestHelpers.MessageGenerator loader2 = StreamTestHelpers.createDefaultLoaderRunnable(stream2);
            Thread loaderThread2 = new Thread(loader2);

            loaderThread1.start();
            loaderThread2.start();

            loaderThread1.join();
            loaderThread2.join();

        } finally {
            tickDB.close();
            tickDB.format();
        }
    }

    @After
    public void shutdown() throws Exception {
        // This is needed to remove special local file system from a global singleton to avoid affecting other tests
        FSFactory.forceSetLocalFS(null);
    }

    private static String        getTemporaryLocation() {
        return getTemporaryLocation("tickdb");
    }

    private static String        getTemporaryLocation(String subpath) {
        File random = Home.getFile("build" + File.separator + "test_temp_db" + File.separator + new GUID().toString() + File.separator + subpath);
        if (random.mkdirs())
            random.deleteOnExit();

        return random.getAbsolutePath();
    }
}