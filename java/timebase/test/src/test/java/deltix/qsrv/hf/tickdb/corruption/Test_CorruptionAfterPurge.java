package deltix.qsrv.hf.tickdb.corruption;

import deltix.qsrv.QSHome;
import deltix.qsrv.hf.pub.ChannelPerformance;
import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.Introspector;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.TDBRunner;
import deltix.qsrv.hf.tickdb.comm.server.TestServer;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.timebase.api.messages.universal.PackageHeader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Category(TickDB.class)
public class Test_CorruptionAfterPurge {

    private static TDBRunner runner;

    @BeforeClass
    public static void start() throws Throwable {
        //System.setProperty(TickDBFactory.VERSION_PROPERTY, "5.0");

        String location = TDBRunner.getTemporaryLocation("timebase");
        if (runner == null) {
            File tb = new File(location);
            QSHome.set(tb.getParent());

            DataCacheOptions options = new DataCacheOptions(Integer.MAX_VALUE, 500 * 1024 * 1024);
            options.fs.withMaxFolderSize(10).withMaxFileSize(1024 * 1024);

            runner = new TDBRunner(true, true, tb.getAbsolutePath(), new TestServer(options, new File(location)));
            runner.startup();
        }
    }

    @AfterClass
    public static void stop() throws Throwable {
        if (runner != null) {
            runner.shutdown();
            runner = null;
        }
    }

    @Test
    public void testLoadAndPurge() {
        long currentTime = LocalDateTime.of(2019, 10, 29, 13, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();

        for (int i = 0; i < 100; ++i) {
            DXTickDB db = runner.getTickDb();
            String streamName = "SIM_EXCH";
            DXTickStream stream = getOrCreateStream(db, streamName, PackageHeader.class);
            DeterminedLoader loader = new DeterminedLoader(
                new PackageHeaderLoader(createLoader(stream), "BTCUSD", "EXCH"),
                currentTime, 50000
            );
            currentTime = loader.load(50000);

            stream.purge(currentTime - 100);

            currentTime = loader.load(50000);
            loader.close();

            checkReadable(streamName);
        }
    }

    public static TickLoader createLoader(DXTickStream stream) {
        LoadingOptions options = new LoadingOptions();
        options.channelQOS = ChannelQualityOfService.MAX_THROUGHPUT;

        return stream.createLoader(options);
    }

    private void checkReadable(String streamName) {
        DXTickDB db = runner.getTickDb();
        DXTickStream stream = getOrCreateStream(db, streamName, PackageHeader.class);

        SelectionOptions options = new SelectionOptions(false, false);
        options.ordered = true;
        options.reversed = true;
        options.channelPerformance = ChannelPerformance.MIN_CPU_USAGE;
        options.channelQOS = ChannelQualityOfService.MIN_INIT_TIME;

        try (TickCursor cursor = stream.select(Long.MAX_VALUE - 1, options)) {
            int count = 0;
            while (cursor.next()) {
                InstrumentMessage msg = cursor.getMessage();
                System.out.println("Read " + streamName + " stream succeeded " + msg.getTimeStampMs());
                break;
            }
        }
    }

    private static DXTickStream getOrCreateStream(DXTickDB db, String key, Class<?>... classes) {
        return getOrCreateStream(db, key, StreamScope.DURABLE, classes);
    }

    private static DXTickStream getOrCreateStream(DXTickDB db, String key, StreamScope scope, Class<?>... classes) {
        DXTickStream stream = db.getStream(key);
        if (stream == null) {
            System.out.println("Stream " + key + " not found");
            System.out.println("Creating new stream.");

            StreamOptions options = new StreamOptions(scope, key, "", 1);
            options.setPolymorphic(introspectClasses(classes));
            stream = db.createStream(key, options);

            System.out.println("Stream " + key + " created.");
        } else {
            System.out.println("Stream " + key + " found.");
        }

        return stream;
    }

    public static RecordClassDescriptor[] introspectClasses(Class<?>... classes) {
        try {
            Introspector ix = Introspector.createEmptyMessageIntrospector();
            RecordClassDescriptor[] rcds = new RecordClassDescriptor[classes.length];
            for (int i = 0; i < classes.length; ++i) {
                rcds[i] = ix.introspectRecordClass(classes[i]);
            }

            return rcds;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
