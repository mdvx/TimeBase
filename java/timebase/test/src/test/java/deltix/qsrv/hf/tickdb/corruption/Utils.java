package deltix.qsrv.hf.tickdb.corruption;

import com.sun.management.HotSpotDiagnosticMXBean;
import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.qsrv.hf.pub.md.Introspector;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.timebase.api.messages.universal.PackageHeader;

import javax.management.MBeanServer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static TickLoader createLoader(DXTickStream stream) {
        LoadingOptions options = new LoadingOptions();
        options.channelQOS = ChannelQualityOfService.MAX_THROUGHPUT;

        return stream.createLoader(options);
    }

    public static List<LoaderThread> createLoaders(DXTickDB db, int count) {
        List<LoaderThread> loaders = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            DXTickStream stream = Utils.getOrCreateTestStream(db, i);
            loaders.add(new LoaderThread(
                "MD LOADER #" + i,
                PackageHeaderLoader.createComposite(
                    Utils.createLoader(stream),
                    "EXCH" + i,
                    "BTCUSD", "LTCETH", "BTCGPB", "BTCETH", "USDGBP", "BYNRUB"
                ),
                5, 200
            ));
        }

        return loaders;
    }

    public static List<DeterminedLoader> createDeterminedLoaders(DXTickDB db, int count, long startTime, int messagesPerSecond) {
        List<DeterminedLoader> loaders = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            DXTickStream stream = Utils.getOrCreateTestStream(db, i);
            loaders.add(
                new DeterminedLoader(
                    PackageHeaderLoader.createComposite(
                        Utils.createLoader(stream),
                        "EXCH" + i,
                        "BTCUSD", "LTCETH", "BTCGPB", "BTCETH", "USDGBP", "BYNRUB"
                    ), startTime, messagesPerSecond
                )
            );
        }

        return loaders;
    }

    public static DXTickStream getOrCreateTestStream(DXTickDB db, int i) {
        return getOrCreateStream(db, "MD#" + i, PackageHeader.class);
    }

    public static DXTickStream getOrCreateStream(DXTickDB db, String key, Class<?>... classes) {
        return getOrCreateStream(db, key, StreamScope.DURABLE, classes);
    }

    public static DXTickStream getOrCreateStream(DXTickDB db, String key, StreamScope scope, Class<?>... classes) {
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

    private static RecordClassDescriptor[] introspectClasses(Class<?>... classes) {
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

    public static void dumpHeap(String filePath) throws IOException {
        System.out.println("Writing " + filePath);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
            server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        mxBean.dumpHeap(filePath, true);
    }

    public static void dumpThreads(String filePath) throws IOException {
        System.out.println("Writing " + filePath);

        ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (final ThreadInfo info : threads) {
                writer.write(info.toString());
            }
        }
    }

}
