package deltix.qsrv.hf.tickdb;

import deltix.qsrv.hf.tickdb.server.ServerRunner;
import deltix.qsrv.hf.tickdb.util.ZIPUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.comm.client.TickDBClient;
import deltix.timebase.api.messages.securities.*;
import deltix.util.lang.Util;
import deltix.util.io.Home;

import java.io.File;
import java.io.FileInputStream;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

/**
 * Date: Jun 9, 2010
 *
 * @author alex
 */
@Category(TickDBFast.class)
public class Test_DF1 {

    private static File STREAM_FILE = Home.getFile("//testdata//qsrv//hf//tickdb//securities.zip");

    private static TDBRunner runner;

    @BeforeClass
    public static void      start() throws Throwable {
        File folder = new File(TDBRunner.getTemporaryLocation());

        FileInputStream is = new FileInputStream(STREAM_FILE);
        ZIPUtil.extractZipStream(is, folder);
        is.close();

        runner = new ServerRunner(true, false, folder.getAbsolutePath());
        runner.startup();
    }

    @AfterClass
    public static void      stop() throws Throwable {
        runner.shutdown();
        runner = null;
    }

    @Test
    public void testSecurityStream1() {
        DXTickDB client = runner.getTickDb();
        String name = String.valueOf("securities");
        DXTickStream stream = client.getStream(name);

        TickLoader loader = null;
        try {
            loader = stream.createLoader();

            Index index = new Index();
            index.setSymbol("ETF");
            loader.send(index);

            loader.close();
        } finally {
            Util.close(loader);
        }

        TickCursor cursor = stream.select(0, null);
        try {
            assertTrue(cursor.next());
            assertTrue(cursor.next());
            assertTrue(stream.listEntities().length > 1);
        } finally {
            Util.close(cursor);
        }
    }

    public void testWrite() {
        DXTickDB db = runner.getServerDb();
        String name = String.valueOf("sss");
        DXTickStream stream = db.createStream (name, name, name, 1);
        StreamConfigurationHelper.setMeta(stream);

        TickLoader loader = null;
        try {
            LoadingOptions loadingOptions = new LoadingOptions();
            loader = stream.createLoader(loadingOptions);

            Index index = new Index();
            index.setSymbol("XXL");
            index.setTimeStampMs(System.currentTimeMillis());
            loader.send(index);

        } finally {
            Util.close(loader);
        }

        db.close();
        db.open(false);

        stream = db.getStream (name);

        try {
            LoadingOptions loadingOptions = new LoadingOptions();
            loader = stream.createLoader(loadingOptions);

            Equity index = new Equity();
            index.setSymbol("XXX");
            index.setTimeStampMs(System.currentTimeMillis());
            loader.send(index);

        } finally {
            Util.close(loader);
        }
    }

    @Test
    public void testSecurityStream() {
        TickDBClient client = (TickDBClient) runner.getTickDb();
        String name = String.valueOf("securities1");
        DXTickStream stream = client.createStream (name, name, name, 1);
        StreamConfigurationHelper.setMeta(stream);

        TickLoader loader = null;
        try {
            loader = stream.createLoader();
            Equity equity = new Equity();
            equity.setSymbol("MSFT");
            loader.send(equity);

            Future future = new Future();
            future.setSymbol("MG");
            loader.send(future);

            ETF etf = new ETF();
            etf.setSymbol("ETF");
            loader.send(etf);

            loader.close();
        } finally {
            Util.close(loader);
        }
       
        SelectionOptions options = new SelectionOptions();
        options.reversed = true;
        TickCursor cursor = stream.select(Long.MIN_VALUE, options);
        cursor.next();

        stream.clear();

        try {
            loader = stream.createLoader();
            Equity equity = new Equity();
            equity.setSymbol("IBM");
            loader.send(equity);

            ETF etf = new ETF();
            etf.setSymbol("ETF");
            loader.send(etf);

            loader.close();
            loader = null;
        } finally {
            Util.close(loader);
        }

        stream.clear();

        try {
            loader = stream.createLoader();
            Equity equity = new Equity();
            equity.setSymbol("IBM");
            loader.send(equity);

            ETF etf = new ETF();
            etf.setSymbol("ETF");
            loader.send(etf);

            loader.close();
            loader = null;
        } finally {
            Util.close(loader);
            Util.close(cursor);
        }
    }
    
    
}
