package deltix.qsrv.hf.tickdb.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.impl.FriendlyStream;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.qsrv.hf.tickdb.pub.DataCacheOptions;
import deltix.qsrv.hf.tickdb.pub.StreamOptions;
import deltix.qsrv.hf.tickdb.pub.StreamScope;
import deltix.qsrv.hf.tickdb.pub.TickDBFactory;
import deltix.qsrv.hf.tickdb.replication.ReplicationOptions;
import deltix.qsrv.hf.tickdb.replication.RestoreOptions;
import deltix.qsrv.hf.tickdb.replication.StreamReplicator;
import deltix.qsrv.hf.tickdb.replication.StreamStorage;
import deltix.qsrv.hf.tickdb.tool.StreamComparer.ComparerType;
import deltix.util.cmdline.DefaultApplication;
import deltix.util.io.IOUtil;
import deltix.util.lang.StringUtils;
import deltix.util.lang.Util;

public class TBMigrator extends DefaultApplication {
    public static final Logger LOGGER = Logger.getLogger("deltix.qsrv.hf.tickdb.tool");

    public TBMigrator(String[] args) {
        super(args);
    }

    @Override
    protected void run() throws Throwable {
        String qsHome = getMandatoryArgValue("-home");

        File qshomeDir = new File(qsHome);
        if (!qshomeDir.exists())
            throw new IllegalArgumentException("QuantServerHome does not exist: " + qshomeDir.getAbsolutePath());

        String streamsRegexp = getArgValue("-streamsRegexp");
        if (streamsRegexp == null) {
            String streamsValue = getArgValue("-streams");
            String[] streams = !StringUtils.isEmpty(streamsValue) ? StringUtils.split(streamsValue, ",", true, true) : null;
            if (streams != null && streams.length > 0) {
                streamsRegexp = "";
                for (int i = 0; i < streams.length; i++)
                    streamsRegexp += ( i == 0 ? "(" : "|(") + streams[i] + ")";
            }
        }

        File tickdbFrom = new File(qshomeDir, "tickdb");
        File tickdbTo = new File(qshomeDir, "timebase");
        TBStreamMigrator migrator = new TBStreamMigrator(tickdbFrom, tickdbTo,
                                                         isArgSpecified("-restore"),
                                                         streamsRegexp,
                                                         new DataCacheOptions(),
                                                         isArgSpecified("-compare"),
                                                         isArgSpecified("-ordered") ? ComparerType.Ordered : ComparerType.Unordered,
                                                         !isArgSpecified("-transient"),
                                                         getArgValue("-hdfs"));

        migrator.migrate();
    }

    @Override
    public void handleException(Throwable x) {
        super.handleException(x);
        printUsageAndExit();
    }

    public static void main(String[] args) {
        new TBMigrator(args).start();
    }

    ///////////////////////////// HELPER CLASS //////////////////////////

    public static final class TBStreamMigrator {

        private final File srcTBLocation;
        private final File dstTBLocation;
        private final boolean restore;
        private final String streamsRegexp;
        private final DataCacheOptions dataCacheOptions;
        private final boolean compare;
        private final ComparerType comparerType;
        private final boolean skipNonDurable;
        private final String hdfs;

        public TBStreamMigrator(File srcTBLocation, File dstTBLocation,
                                boolean restore,
                                String streamsRegexp,
                                DataCacheOptions dataCacheOptions,
                                boolean compare, StreamComparer.ComparerType comparerType,
                                boolean skipNonDurable,
                                String hdfs) {
            this.srcTBLocation = srcTBLocation;
            this.dstTBLocation = dstTBLocation;
            this.restore = restore;
            this.streamsRegexp = streamsRegexp;
            this.dataCacheOptions = dataCacheOptions;
            this.compare = compare;
            this.comparerType = comparerType;
            this.skipNonDurable = skipNonDurable;
            this.hdfs = hdfs;
        }

        public void migrate() throws IOException {
            LOGGER.log(Level.INFO, "Migrate streams from \"{0}\" to \"{1}\"", new Object[]{srcTBLocation, dstTBLocation});

            try (DXTickDB tickdbSrc = TickDBFactory.create(dataCacheOptions, srcTBLocation)) {
                tickdbSrc.open(false);

                DXTickStream[] streams = listStreams(tickdbSrc, streamsRegexp);
                StreamReplicator replicator = new StreamReplicator();

                IOUtil.mkDirIfNeeded(dstTBLocation);
                DataCacheOptions options = new DataCacheOptions();
                options.fs.url = hdfs;

                try (DXTickDB tickdbDst = TickDBFactory.create(options, dstTBLocation)) {
                    tickdbDst.open(false);

                    StreamComparer comparer = StreamComparer.create(comparerType);

                    for (int i = 0; i < streams.length; i++) {
                        final DXTickStream stream = streams[i];
                        final String streamKey = stream.getKey();

                        if (stream.getScope() == StreamScope.DURABLE) {
                            LOGGER.log(Level.INFO, "Start [{0}] stream migration...", streamKey);

                            StreamStorage from = new StreamStorage(tickdbSrc, streamKey);
                            StreamStorage to = new StreamStorage(tickdbDst, streamKey);

                            if (restore) {
                                replicator.restore(from, to, new RestoreOptions(streamKey));
                            } else {

                                // listEntities returns sorted list
                                IdentityKey[] entities = from.getSource().listEntities();

                                DXTickStream target = to.getSource();

                                if (target == null) {
                                    StreamOptions so = from.getSource().getStreamOptions();
                                    so.name = to.name;
                                    so.version = "5.0";
                                    target = to.db.createStream(to.name, so);

                                    if (target instanceof FriendlyStream) {
                                        FriendlyStream s = (FriendlyStream) target;
                                        for (IdentityKey entity : entities)
                                            s.addInstrument(entity);
                                    }
                                }

                                replicator.replicate(from, to, new ReplicationOptions());
                            }

                            if (compare && !comparer.compare(from, to)) {
                                LOGGER.log(Level.SEVERE, "[{0}] Source and migrated streams are different!", streamKey);
                            }
                        } else if (!skipNonDurable) {
                            if (tickdbDst.getStream(streamKey) == null) {
                                LOGGER.log(Level.INFO, "Create [{0}] non-durable stream", streamKey);
                                StreamOptions so = stream.getStreamOptions();
                                tickdbDst.createStream(streamKey, so);
                            } else {
                                LOGGER.log(Level.WARNING, "Non-durable [{0}] stream is already exist - skip creation.", streamKey);
                            }

                        }
                    }
                }
            }

            LOGGER.info("Streams migration successfully finished.");
        }

        private static DXTickStream[] listStreams(DXTickDB tickdb, String streamRegexp) {
            Pattern pattern = streamRegexp != null ? Pattern.compile(streamRegexp) : null;

            DXTickStream[] streams = tickdb.listStreams();
            List<DXTickStream> tbstreams = new ArrayList<>(streams.length);
            for (int i = 0; i < streams.length; i++) {
                String streamKey = streams[i].getKey();
                if (streamKey.equalsIgnoreCase(TickDBFactory.EVENTS_STREAM_NAME)) // skip #events
                    continue;

                DXTickStream tbstream = tickdb.getStream(streamKey);
                if (tbstream == null) {
                    LOGGER.log(Level.WARNING, "Skip unknown stream [{0}]", streamKey);
                    continue;
                }

                if (pattern == null || pattern.matcher(streamKey).matches())
                    tbstreams.add(tbstream);
            }
            return tbstreams.toArray(new DXTickStream[tbstreams.size()]);
        }
    }
}
