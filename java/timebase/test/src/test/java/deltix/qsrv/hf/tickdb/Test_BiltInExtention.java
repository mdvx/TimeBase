package deltix.qsrv.hf.tickdb;

import deltix.qsrv.hf.tickdb.util.ZIPUtil;
import deltix.util.StringUtil;
import deltix.util.io.Home;
import deltix.util.lang.StringUtils;
import deltix.util.lang.Util;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import deltix.qsrv.hf.tickdb.pub.*;
import deltix.timebase.api.messages.TradeMessage;

import deltix.qsrv.hf.pub.ExchangeCodec;
import deltix.util.io.BasicIOUtil;
import deltix.util.io.IOUtil;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

/**
 * User: BazylevD
 * Date: Jan 6, 2010
 * Time: 7:37:26 PM
 */
@Category(TickDBFast.class)
public class Test_BiltInExtention {
    private static final File DIR = Home.getFile("testdata", "qsrv", "hf", "tickdb");
    private static final File ZIP = new File(DIR, "tickdb.ticksEx.zip");

    protected DXTickDB db = null;

    @Before
    public void         setUp () throws IOException, InterruptedException {
        File folder = new File (TDBRunner.getTemporaryLocation());
        BasicIOUtil.deleteFileOrDir(folder);
        folder.mkdirs();
        FileInputStream is = new FileInputStream(ZIP);
        ZIPUtil.extractZipStream(is, folder);
        is.close();

        db = TickDBFactory.create(folder);
        db.open(false);
    }

    @After
    public void         tearDown () {
        db.close();
    }

    /*
     * Test that bounded decoder and encoder handle the extended fields correctly.
     */
    @Test
    public void testReadWrite() throws IOException, InterruptedException {
        DXTickStream stream = db.getStream("ticksEx");

        final TickLoader loader = stream.createLoader();
        try {
            TradeMessage msg = new TradeMessage();
            msg.setSymbol("IBM");
            msg.setTimeStampMs(1262304000000L); // 2010-01-01
            msg.setPrice(0.1);
            msg.setSize(1);
            msg.setExchangeId(ExchangeCodec.codeToLong("UN"));
            msg.setCurrencyCode((short) 999);
            loader.send(msg);
        } finally {
            loader.close();
        }

        final TickDBUtil util = new TickDBUtil();
        TickCursor cursor = TickCursorFactory.create(stream, 0);
        final String output = util.toString(cursor);
        Util.close(cursor);

        //TickDBUtil.dump2File(TickDBUtil.USER_HOME + "ticksEx.txt", output);
        final String[] lines = IOUtil.readLinesFromTextFile (new File (DIR, "ticksEx.txt"));
        String etalon = StringUtils.join(System.lineSeparator(), lines);
        Assert.assertEquals("Data log is not the same as etalon", etalon, output);
    }

    /*
     * Test that bounded and unbound decoders handle legacy data correctly after extention.
     */
    @Test
    public void testReadJustExtended() throws IOException, InterruptedException {
        DXTickStream stream = db.getStream("ticksJustExtended");

        final TickLoader loader = stream.createLoader();
        try {
            TradeMessage msg = new TradeMessage();
            msg.setSymbol("IBM");
            msg.setTimeStampMs(1262304000000L); // 2010-01-01
            msg.setPrice(0.1);
            msg.setSize(1);
            msg.setExchangeId(ExchangeCodec.codeToLong("UN"));
            loader.send(msg);
        } finally {
            loader.close();
        }

        final TickDBUtil util = new TickDBUtil();

        // bound case
        TickCursor cursor = TickCursorFactory.create(stream, 0);
        String output = util.toString(cursor);
        Util.close(cursor);
        
        //TickDBUtil.dump2File(TickDBUtil.USER_HOME + "ticksEx.txt", output);
        String[] lines = IOUtil.readLinesFromTextFile(new File (DIR, "ticksEx.txt"));
        String etalon = StringUtils.join(System.lineSeparator(), lines);
        Assert.assertEquals("Data log is not the same as etalon", etalon, output);

        // unbound case
        cursor = TickCursorFactory.create(stream, 0, new SelectionOptions(true, false));
        output = util.toString(cursor);
        Util.close(cursor);
        //TickDBUtil.dump2File(TickDBUtil.USER_HOME + "ticksJustExtended.Unbound.txt", output);
        lines = IOUtil.readLinesFromTextFile(new File (DIR, "ticksJustExtended.Unbound.txt"));
        etalon = StringUtils.join(System.lineSeparator(), lines);
        Assert.assertEquals("Data log is not the same as etalon", etalon, output);
    }
}
