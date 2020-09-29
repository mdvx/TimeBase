package deltix.qsrv.hf.tickdb.tb5;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.stream.MessageFileHeader;
import deltix.qsrv.hf.stream.Protocol;
import deltix.qsrv.hf.tickdb.TDBRunner;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.server.ServerRunner;
import deltix.qsrv.hf.tickdb.ui.tbshell.TickDBShell;
import deltix.util.JUnitCategories;
import deltix.util.io.Home;
import deltix.util.time.GMT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static deltix.qsrv.hf.tickdb.TDBRunner.getTemporaryLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(JUnitCategories.TickDBFast.class)
public class Test_Split {

    private static TDBRunner runner;

    private static File DATA_FILE = Home.getFile("testdata//tickdb//misc//charts.qsmsg.gz");

    @BeforeClass
    public static void start() throws Throwable {
        String location = getTemporaryLocation();
        DataCacheOptions options = new DataCacheOptions();
        options.fs = new FSOptions();
        options.fs.maxFileSize = 1024 * 1024;
        options.fs.maxFolderSize = 10;

        runner = new ServerRunner(true, true, location);
        runner.startup();
    }

    @AfterClass
    public static void stop() throws Throwable {
        runner.shutdown();
        runner = null;
    }

    @Test
    public void testLoad() throws IOException {
        MessageFileHeader header = Protocol.readHeader(DATA_FILE);

        String name = "charts";
        DXTickStream stream = runner.getTickDb().createStream(name,
                StreamOptions.polymorphic(StreamScope.DURABLE, name, name, 0, header.getTypes()));

        TickDBShell.loadMessageFile(DATA_FILE, stream);

        long[] range = stream.getTimeRange();

        assert range != null;

        System.out.println("Time range:" + GMT.formatDateTimeMillis(range[0]) + "-" + GMT.formatDateTimeMillis(range[1]));

        IdentityKey[] ids = stream.listEntities();
        for (IdentityKey id : ids)
            checkRange(stream, id);

        checkRange(stream);
    }

    private void checkRange(DXTickStream stream, IdentityKey ... ids) {
        long[] range = stream.getTimeRange(ids);

        try (TickCursor cursor = stream.select(0, new SelectionOptions(true, false), null, ids.length > 0 ? ids : null)) {
            if (range != null) {
                assertTrue(cursor.next());
                assertTrue(cursor.getMessage().getTimeStampMs() == range[0]);

                while (cursor.next()) ;

                long lastTime = cursor.getMessage().getTimeStampMs();

                assertEquals(GMT.formatDateTimeMillis(range[1]) + " vs " + GMT.formatDateTimeMillis(lastTime),
                        range[1], cursor.getMessage().getTimeStampMs());
            } else {
                assertFalse("Stream have data for " + Arrays.toString(ids), cursor.next());
            }
        }
    }
}
