package deltix.qsrv.hf.tickdb;

import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.stream.MessageFileHeader;
import deltix.qsrv.hf.stream.Protocol;
import deltix.qsrv.hf.tickdb.pub.*;

import deltix.timebase.messages.*;

import deltix.qsrv.hf.tickdb.server.ServerRunner;
import deltix.timebase.api.messages.securities.ETF;
import deltix.timebase.api.messages.securities.Equity;
import deltix.timebase.api.messages.securities.Index;
import deltix.qsrv.hf.tickdb.ui.tbshell.TickDBShell;
import deltix.qsrv.testsetup.TickDBCreator;
import deltix.util.io.Home;
import deltix.util.io.UncheckedIOException;
import deltix.util.lang.Util;
import deltix.util.time.GMT;
import deltix.util.time.Interval;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import static junit.framework.Assert.assertEquals;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

@Category(TickDBFast.class)
public class Test_TickLoader {
       
    private static TDBRunner runner;

    @BeforeClass
    public static void start() throws Throwable {
        runner = new ServerRunner(true, false);
        runner.startup();
    }

    @AfterClass
    public static void stop() throws Throwable {
        runner.shutdown();
        runner = null;
    }

//    @Test
//    public void generateTestData() {
//        DXTickDB db = getTickDb();
//        StreamOptions               options =
//                new StreamOptions (StreamScope.DURABLE, null, null, 0);
//
//        options.setFixedType (StreamConfigurationHelper.
//                mkBBOMessageDescriptor(null, false, null, null,
//                    FloatDataType.ENCODING_SCALE_AUTO,
//                    FloatDataType.ENCODING_SCALE_AUTO
//                ));
//        GregorianCalendar c = new GregorianCalendar(2009, 1, 1);
//
//        Test_SchemaConverter.BBOGenerator generator =
//                new Test_SchemaConverter.BBOGenerator(
//                        c, (int) BarMessage.BAR_SECOND, -1, "MSFT", "ORCL", "IMB", "AAPL");
//
//        final DXTickStream      stream = db.createStream ("test", options);
//
//        TickLoader        loader = null;
//        try {
//            loader = stream.createLoader ();
//
//            while (c.get(GregorianCalendar.YEAR) < 2010) {
//                generator.next();
//                loader.send(generator.getMessage());
//            }
//            loader.saveChanges();
//            loader.close();
//        }
//        finally {
//            Util.close(loader);
//        }
//
//        db.waitForClosedLoaders(100000);
//    }

    @Test
    public void testClearAll() throws InterruptedException {
       String name = getClass().getSimpleName() + ".testClearAll";
       testClear(name);
    }

    @Test
    public void testClear() throws InterruptedException {
        String name = getClass().getSimpleName() + ".testClear";
        testClear(name, new InstrumentKey("ORCL"));
    }

    @Test
    public void testDeleteAll() {
        testDeleteData();
    }

    @Test
    public void testDeleteSome() {
        testDeleteData(new InstrumentKey("ORCL"), new InstrumentKey("AAPL"));
    }

    public DXTickDB     getTickDb() {
        return runner.getTickDb();
    }

    @Test
    public void testDeleteStream() {        
        DXTickDB db = getTickDb();
        StreamOptions               options =
                new StreamOptions (
                    StreamScope.DURABLE,
                    "Stream Name",
                    "Description Line1\nLine 2\nLine 3",
                    1
                );

        options.setFixedType (StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ());

        TDBRunner.TradesGenerator generator =
                new TDBRunner.TradesGenerator(
                    new GregorianCalendar(2009, 1, 1),
                        (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100_000, "DLTX", "ORCL");

        boolean passed = false;

        final DXTickStream      stream = db.createStream ("test", options);

        int count = 0;

        TickLoader        loader = null;
        try {
             loader = stream.createLoader ();

            while (generator.next()) {
                loader.send(generator.getMessage());
                count++;
                if (count == 20_000) {
                    ((Flushable)loader).flush();
                    stream.clear();
                }
            }
            loader.close();
        }
        catch (WriterAbortedException e) {
            // valid case
            passed = true;
        }
        catch (WriterClosedException e) {
            // valid case
            passed = true;
        }
        catch (UncheckedIOException e) {
            // valid case
            passed = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.close(loader);
        }

        count = 0;
        try (TickCursor cursor = stream.select(Long.MIN_VALUE, new SelectionOptions(false, false))) {
            while (cursor.next())
                count++;
        }

        if (!passed)
            assertEquals(count, 80_000);
        else
           assertTrue(passed);
    }

    @Test
    public void testDeleteStream1() {
        DXTickDB db = getTickDb();

        StreamOptions               options =
                new StreamOptions (
                        StreamScope.DURABLE,
                        "Stream Name",
                        "Description Line1\nLine 2\nLine 3",
                        1
                );

        options.setFixedType (StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ());

        TDBRunner.TradesGenerator generator =
                new TDBRunner.TradesGenerator(
                        new GregorianCalendar(2009, 1, 1),
                        (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100000, "DLTX", "ORCL");

        final DXTickStream      stream = db.createStream ("test", options);
        try (TickLoader loader = stream.createLoader ()) {
            while (generator.next())
                loader.send(generator.getMessage());
        }

        boolean passed = false;

        try (TickCursor cursor = stream.select(Long.MIN_VALUE, new SelectionOptions(false, true))) {
            int count = 0;
            while (cursor.next()) {
                if (count++ == 20000)
                    stream.delete();
            }
        } catch (Exception ex) {
            passed = true;
        }

        assertTrue(passed);
    }

    @Ignore
    public void testLoadAndClear() throws InterruptedException, IOException {
        DXTickDB tickdb = runner.getServerDb();

        String name = "testLoadAndClear";

        DXTickStream stream = tickdb.getStream(name);
        if (stream != null)
            stream.delete();

        File file = Home.getFile("/testdata/tickdb/brokertec.L2.qsmsg.gz");
        stream = createStreamFromFile(name, file);

        stream.clear(stream.listEntities());
        TickDBShell.loadMessageFile(file, stream);
    }

    private static DXTickStream         createStreamFromFile(String name, File from) throws IOException {
        MessageFileHeader header = Protocol.readHeader(from);

        StreamOptions options = new StreamOptions();
        options.name = name;
        if (header.getTypes().length > 1)
            options.setPolymorphic(header.getTypes());
        else
            options.setFixedType(header.getTypes()[0]);

        DXTickStream stream = runner.getTickDb().createStream(name, options);
        TickDBShell.loadMessageFile(from, stream);

        return stream;
    }

    public void testDeleteData(IdentityKey... ids) {
        DXTickDB tickdb = getTickDb();

        String name = getClass().getSimpleName() + ".testDelete";

         if (tickdb.getStream(name) != null)
            tickdb.getStream(name).delete();

        TickDBCreator.createBarsStream(tickdb, name);

        DXTickStream stream = tickdb.getStream(name);

        if (ids.length == 0)
            stream.clear();
        else
            stream.truncate(0, ids);

        assertTrue(stream.getTimeRange(ids) == null);
    }

    @Test
    public void testTruncateFiles() throws Exception {
        DXTickDB tickdb = getTickDb();
        
        String name = getClass().getSimpleName() + ".testTruncateFiles";
        DXTickStream            stream = tickdb.createStream (name, name, name, 2);

        StreamConfigurationHelper.setBar (
            stream, "", null, Interval.MINUTE,
            "DECIMAL(4)",
            "DECIMAL(0)"
        );

        TDBRunner.BarsGenerator gn = new TDBRunner.BarsGenerator(
                new GregorianCalendar(2009, 1, 1), (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 10000,
                "AAPL", "MSFT");

        try (TickLoader loader = stream.createLoader ()) {
             while (gn.next())
                loader.send(gn.getMessage());
        }
        
        //DXTickStream stream = TickDBCreator.createBarsStream(tickdb, name, 2);
        
        TickLoader loader = stream.createLoader();
        long[] range = stream.getTimeRange();

        assert range != null;

        long time = range[0] + (long)(0.5 * (range[1] - range[0] - stream.getPeriodicity().getInterval().toMilliseconds()));
        //long time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2005-03-24 14:30:00").getTime();
        //long time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2005-02-04 18:35:00").getTime();

        try (TickCursor tickCursor = TickCursorFactory.create(stream, time)) {
            if (tickCursor.next())
                time = tickCursor.getMessage().getTimeStampMs();
        }

        Random rnd = new Random(2009);
        deltix.timebase.api.messages.BarMessage message = new deltix.timebase.api.messages.BarMessage();
        message.setSymbol("ORCL");
        message.setTimeStampMs(time);

        message.setHigh(rnd.nextDouble()*100);
        message.setOpen(message.getHigh() - rnd.nextDouble()*10);
        message.setClose(message.getHigh() - rnd.nextDouble()*10);
        message.setLow(Math.min(message.getOpen(), message.getClose()) - rnd.nextDouble()*10);
        message.setVolume(100);
        message.setCurrencyCode((short)840);
        loader.send(message);
        
        loader.close();
        
        long[] timeRange = stream.getTimeRange(new ConstantIdentityKey("ORCL"));

        assertTrue("Truncate to " + GMT.formatDateTimeMillis(time) + " but stream last time = " +
                GMT.formatDateTimeMillis(timeRange[1]), timeRange[1] == time);
    }

    public void testClear(String name, IdentityKey... ids) throws InterruptedException {
        DXTickDB tickdb = runner.getServerDb();

        DXTickStream stream = tickdb.getStream(name);
        if (stream != null)
            stream.delete();

        TickDBCreator.createBarsStream(tickdb, name);

        stream = tickdb.getStream(name);
        
        long[] range = stream.getTimeRange();

        long time = range[0] + (long)(0.5 * (range[1] - range[0] - stream.getPeriodicity().getInterval().toMilliseconds()));
        long lastTime = 0;

        TickCursor tickCursor = null;
        try {
            tickCursor = TickCursorFactory.create(stream, time, ids.length > 0 ? ids : null);
            if (tickCursor.next())
                time = lastTime = tickCursor.getMessage().getTimeStampMs();
            while (time == lastTime && tickCursor.next())
                time = tickCursor.getMessage().getTimeStampMs();
            
        } finally {
            Util.close(tickCursor);
        }

        stream.truncate(time, ids);
        long[] timeRange = stream.getTimeRange(ids);

        assert timeRange != null;

//        assertTrue("Truncate to " + GMT.formatDateTimeMillis(time) + " but stream last time = " +
//                GMT.formatDateTimeMillis(timeRange[1]), timeRange[1] == lastTime);
    }

    public void testImport() throws IOException, InterruptedException {
        File toImport1 = new File("D:\\Workshop\\Projects\\Deltix\\QuantServers\\main\\timerange\\CurrencyShares Daily Bars NYSE.qsmsg.gz");
        File toImport2 = new File("D:\\Workshop\\Projects\\Deltix\\QuantServers\\main\\timerange\\CurrencyShares Daily Bars AMEX.qsmsg.gz");

        MessageFileHeader header = Protocol.readHeader(toImport1);

        StreamOptions options = new StreamOptions();
        options.name = "import";
        options.setFixedType(header.getTypes()[0]);

        DXTickStream stream = getTickDb().createStream("import", options);

        TickDBShell.loadMessageFile(toImport1, stream);
        testRange(stream);

        //long[] range = stream.getTimeRange();
        //System.out.println(GMT.formatDateTimeMillis(range[0]) + ", " + GMT.formatDateTimeMillis(range[1]));

        TickDBShell.loadMessageFile(toImport2, stream);
        testRange(stream);
    }

    public static void testRange(DXTickStream stream) {

        IdentityKey[] entities = stream.listEntities();

        long[] range = stream.getTimeRange();
        long[] rangeAll = stream.getTimeRange(entities);

        for (IdentityKey entity : entities) {
            long[] times = stream.getTimeRange(entity);
            assertNotNull(times);

            //System.out.println("time range for " + entity + ":" + GMT.formatDateTimeMillis(times[0]) + ", " + GMT.formatDateTimeMillis(times[1]));
        }

        assertEquals(GMT.formatDateTimeMillis(range[0]) + " != " + GMT.formatDateTimeMillis(rangeAll[0]), range[0], rangeAll[0]);
        assertEquals(GMT.formatDateTimeMillis(range[1]) + " != " + GMT.formatDateTimeMillis(rangeAll[1]), range[1], rangeAll[1]);
    }

    @Test
    public void testUpdate() {

        DXTickDB tickdb = runner.getServerDb();

        StreamOptions options = new StreamOptions();
        options.name = "test.update";
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());


        DXTickStream stream = tickdb.getStream(options.name);
        if (stream != null)
            stream.delete();

        stream = tickdb.createStream (options.name,  options);

        GregorianCalendar calendar = new GregorianCalendar(2013, 1, 1);

        for (int c = 0; c < 3; c++) {
            calendar.add(Calendar.MONTH, 1);

            TickLoader      loader = stream.createLoader ();

            try {
                Random rnd = new Random(2013);
                for(int i = 0; i < 5000; i++)
                {
                    deltix.timebase.api.messages.BarMessage message = new deltix.timebase.api.messages.BarMessage();
                    message.setSymbol("ES" + (i % 5));
                    message.setTimeStampMs(calendar.getTimeInMillis());

                    message.setHigh(rnd.nextDouble()*100);
                    message.setOpen(message.getHigh() - rnd.nextDouble()*10);
                    message.setClose(message.getHigh() - rnd.nextDouble()*10);
                    message.setLow(Math.min(message.getOpen(), message.getClose()) - rnd.nextDouble()*10);
                    message.setVolume(i);
                    message.setCurrencyCode((short)840);
                    loader.send(message);

                    calendar.add(Calendar.MINUTE, 1);
                }
            } finally {
                Util.close (loader);
            }

            testRange(stream);
        }
    }

    public void testReset() {
        DXTickDB tickdb = getTickDb();
        
        final DXTickStream stream =
            tickdb.createStream ("bars1", "bars1", "bars1", 1);

        StreamConfigurationHelper.setBar (
            stream, "", null, null,
            "DECIMAL(4)",
            "DECIMAL(0)"
        );        

        TickLoader      loader = stream.createLoader ();        
        GregorianCalendar calendar = new GregorianCalendar(2008, 1, 1);

        try {
            Random rnd = new Random(2009);
            for(int i = 0; i < 15; i++)
            {
                deltix.timebase.api.messages.BarMessage message = new deltix.timebase.api.messages.BarMessage();
                message.setSymbol("ES" + (i % 5));

                message.setTimeStampMs(calendar.getTimeInMillis());

                message.setHigh(rnd.nextDouble()*100);
                message.setOpen(message.getHigh() - rnd.nextDouble()*10);
                message.setClose(message.getHigh() - rnd.nextDouble()*10);
                message.setLow(Math.min(message.getOpen(), message.getClose()) - rnd.nextDouble()*10);
                message.setVolume(i);
                message.setCurrencyCode((short)840);
                loader.send(message);

                calendar.add(Calendar.MINUTE, 1);
            }
        } finally {
            Util.close (loader);
        }

        //FeedFilter filter = FeedFilter.createUnrestricted();

        TickCursor cursor = null;

        try {
            cursor = TickCursorFactory.create(stream, 0);

            for (int i = 0; i < 5; i++) {
                cursor.next();
                System.out.println(cursor.getMessage());
            }

            cursor.clearAllEntities();
            cursor.addEntity(new ConstantIdentityKey("ES0"));
//            filter = FeedFilter.createUnrestrictedNoSymbols();
//            filter.addSymbol(InstrumentType.EQUITY, "ES0");
//            cursor.setFilter(filter);

            int count = 0;
            while (cursor.next())
                count++;

            assertTrue("Expected messages = 2, but recieved = " + count, count == 2);

        } finally {
            Util.close(cursor);
        }        
    }
    
    @Test
    public void testLiveTruncate() {

        RecordClassDescriptor rcd = StreamConfigurationHelper.mkUniversalBBOMessageDescriptor();
        
        final DXTickStream stream = runner.getServerDb().createStream("bbo",
                StreamOptions.fixedType(StreamScope.DURABLE, "bbo", null, 0, rcd));

        final int[] errors = new int[1];
        TickLoader loader = null;

        try {
            TDBRunner.BBOGenerator gn = new TDBRunner.BBOGenerator(new GregorianCalendar(2009, 0, 1),
                    (int) deltix.timebase.api.messages.BarMessage.BAR_HOUR, -1, "MSFT", "ORCL", "AAPL");
            
            loader = stream.createLoader();
            loader.addEventListener(new LoadingErrorListener() {                
                 public void onError(LoadingError e) {
                     errors[0]++;
                 }
             });

            long firstTime = gn.getActualTime();
            for (int i = 0; i < 999; i++) {
                if (gn.next())
                    loader.send(gn.getMessage());
            }

            TDBRunner.BBOGenerator gn1 =
                    new TDBRunner.BBOGenerator(
                            new GregorianCalendar(2009, 0, 10), 1000, -1, "MSFT", "ORCL", "AA");
            
            long start = gn1.getActualTime();
            stream.truncate(start);
            
            for (int i = 0; i < 99; i++) {
                if (gn1.next())
                    loader.send(gn1.getMessage());
            }
            loader.close();
            loader = null;
            
            long end = gn1.getActualTime();

            long[] range = stream.getTimeRange(new InstrumentKey("AAPL"));
            assertEquals(firstTime, range[0]);
            assertEquals(GMT.formatDateTimeMillis(start - deltix.timebase.api.messages.BarMessage.BAR_HOUR), GMT.formatDateTimeMillis(range[1]));

            range = stream.getTimeRange(new InstrumentKey("AA"));
            assertEquals(start, range[0]);
            assertEquals(end, range[1]);

            range = stream.getTimeRange(new InstrumentKey("MSFT"));
            assertEquals(firstTime, range[0]);
            assertEquals(end, range[1]);

            TickCursor tickCursor = TickCursorFactory.create(stream, 0);
            assertTrue(tickCursor.next());
            while (tickCursor.next());
            tickCursor.close();

        } finally {
            Util.close(loader);
        }

        assertEquals(0, errors[0]);
    }

    @Test
    public void testSecurityMetaData () {
        DXTickDB tickdb = runner.getServerDb ();

        StreamOptions options = new StreamOptions (StreamScope.DURABLE,
                                                   "securities",
                                                   null,
                                                   1);
        options.setPolymorphic (StreamConfigurationHelper.mkSecurityMetaInfoDescriptors ());

        final DXTickStream stream = tickdb.createStream ("securities",
                                                         options);

        Equity equity = new Equity();
        equity.setSymbol("MSFT");
        equity.setCurrencyCode((short)840);
        equity.setTick(1.0);
        equity.setMultiplier(1.0);

        ETF etf = new ETF();
        etf.setSymbol("MSFT");
        etf.setCurrencyCode((short)840);
        etf.setTick(1.0);
        etf.setMultiplier(1.0);

        TickCursor cursor = TickCursorFactory.create(stream, Long.MIN_VALUE);
        
        loadSecurityMetaData (stream, equity, etf);
        stream.clear ();
        loadSecurityMetaData (stream, equity, etf);
       
        int count = 0;

        TickCursor tickCursor = TickCursorFactory.create(stream, Long.MIN_VALUE);
        while (tickCursor.next ())
            count++;

        Util.close (tickCursor);
        Util.close (cursor);
        
        assertEquals (1, count);
    }

    @Test
    public void testErrors() {
        final DXTickStream stream =
            getTickDb().createStream ("errors", "errors", "errors", 0);

        final int[] errors = new int[1];
        StreamConfigurationHelper.setBar (stream, null, null, null);

        TickLoader loader = null;

        loader = stream.createLoader();
        LoadingErrorListener listener = new LoadingErrorListener() {
            public void onError(LoadingError e) {
                errors[0]++;
            }
        };
        loader.addEventListener(listener);

        deltix.timebase.api.messages.BarMessage msg = new deltix.timebase.api.messages.BarMessage();
        msg.setSymbol("MSFT");

        long time = System.currentTimeMillis();
        msg.setTimeStampMs(time);
        loader.send(msg);
        msg.setTimeStampMs(time + 1);
        loader.send(msg);
        msg.setTimeStampMs(time - 1);
        loader.send(msg);
        msg.setTimeStampMs(time + 2);
        loader.send(msg);
        msg.setTimeStampMs(time + 3);
        loader.send(msg);
        msg.setTimeStampMs(time + 2);
        loader.send(msg);

        loader.close();
        loader.removeEventListener(listener);

        assertEquals(errors[0], 2);
    }

    private void loadSecurityMetaData (final DXTickStream stream,
                                       final Equity equity,
                                       final ETF etf) {
        stream.clear ();
        TickLoader loader = stream.createLoader ();
        try {
            loader.send (equity);
            Util.close(loader);

            stream.clear ();

            loader = stream.createLoader ();
            loader.send (etf);
            
            Util.close(loader);
        } catch (Throwable x) {
            x.printStackTrace ();
            throw new RuntimeException (x);
        } finally {
            Util.close (loader);
        }
    }

    @Test
    public void         testClearSecurities() {
        testClearSecurities(false);

        testClearSecurities(true);
    }

    public void         testClearSecurities(boolean remote) {
        DXTickDB db = remote ? runner.getTickDb() : runner.getServerDb();

        String name = "sss." + remote;
        DXTickStream stream = db.createStream (name, name, name, 1);
        StreamConfigurationHelper.setMeta(stream);

        LoadingOptions loadingOptions = new LoadingOptions();

        final int[] errors = new int[1];
        final LoadingErrorListener listener = new LoadingErrorListener() {
            public void onError(LoadingError e) {
                errors[0]++;
                e.printStackTrace(System.out);
            }
        };

        try (TickLoader loader = stream.createLoader(loadingOptions)) {
            loader.addEventListener(listener);

            for (int i = 0; i < 10; i++) {
                Index index = new Index();
                index.setSymbol("XXL" + i);
                index.setTimeStampMs(0);
                loader.send(index);
            }
        }
        
        int total = 100;
        IdentityKey[] list = new IdentityKey[total];

        try (TickLoader loader = stream.createLoader(loadingOptions)) {

            loader.addEventListener(listener);

            for (int i = 0; i < total; i++) {
                Index index = new Index();
                index.setSymbol("MMX" + i);
                index.setTimeStampMs(1);
                loader.send(index);
                list[i] = new ConstantIdentityKey(index.getSymbol());
            }
        }

        assertEquals(110, stream.listEntities().length);

        // mo errors
        assertEquals(0, errors[0]);

        stream.clear(list);

        assertEquals(10, stream.listEntities().length);
    }

    @Test
    public void         testClearInstruments() {
        DXTickDB db = getTickDb();

        String name = "sss.clear";
        DXTickStream stream = db.createStream (name, name, name, 1);
        StreamConfigurationHelper.setMeta(stream);

        LoadingOptions loadingOptions = new LoadingOptions();
        final int[] errors = new int[1];
        final LoadingErrorListener listener = new LoadingErrorListener() {
            public void onError(LoadingError e) {
                errors[0]++;
                e.printStackTrace(System.out);
            }
        };

        try (TickLoader loader = stream.createLoader(loadingOptions)) {
            loader.addEventListener(listener);

            for (int i = 0; i < 10; i++) {
                Index index = new Index();
                index.setSymbol("XXL" + i);
                index.setTimeStampMs(0);
                loader.send(index);
            }
        }

        try (TickLoader loader = stream.createLoader(loadingOptions)) {
            loader.addEventListener(listener);

            for (int i = 0; i < 100; i++) {
                Index index = new Index();
                index.setSymbol("MMX" + i);
                index.setTimeStampMs(1);
                loader.send(index);
            }
        }
        assertEquals(110, stream.listEntities().length);

        // mo errors
        assertEquals(0, errors[0]);

        stream.clear();

        assertEquals(0, stream.listEntities().length);
    }

    @Test
    public void testAppend() throws InterruptedException {
        StreamOptions options = new StreamOptions (StreamScope.DURABLE, "testAppend", null, 0);
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        final DXTickStream live = runner.getServerDb().createStream("testAppend", options);

        LoadingOptions o = new LoadingOptions();
        o.writeMode = LoadingOptions.WriteMode.APPEND;
        final TickLoader loader = live.createLoader(o);

        final int[] errors = new int[1];
        final LoadingErrorListener listener = new LoadingErrorListener() {
                 public void onError(LoadingError e) {
                     errors[0]++;
                     System.out.println(e);
                 }
             };
        loader.addEventListener(listener);

        TDBRunner.BarsGenerator gn =
            new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100, "ORCL", "WWWW");

        while (gn.next())
            loader.send(gn.getMessage());

        loader.close();

        assertEquals(0, errors[0]);
    }

    @Test
    public void testSpaces() throws Exception {
        testAppend1("append1");
        runner.getServerDb().close();
        runner.getServerDb().open(false);
        testAppend1("append2");
    }

    public void testAppend1(String name) throws InterruptedException {
        StreamOptions options = new StreamOptions (StreamScope.DURABLE, name, null, 0);
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        final DXTickStream live = runner.getTickDb().createStream(name, options);

        LoadingOptions o = new LoadingOptions();
        o.space = "local";
        o.writeMode = LoadingOptions.WriteMode.APPEND;
        try (TickLoader loader = live.createLoader(o)) {

            final int[] errors = new int[1];
            final LoadingErrorListener listener = new LoadingErrorListener() {
                public void onError(LoadingError e) {
                    errors[0]++;
                    System.out.println(e);
                }
            };
            loader.addEventListener(listener);

            TDBRunner.BarsGenerator gn =
                    new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100, "MSFT", "IBM");

            while (gn.next())
                loader.send(gn.getMessage());
        }

        o = new LoadingOptions();
        o.writeMode = LoadingOptions.WriteMode.APPEND;
        try (TickLoader loader = live.createLoader(o)) {

            final int[] errors = new int[1];
            final LoadingErrorListener listener = new LoadingErrorListener() {
                public void onError(LoadingError e) {
                    errors[0]++;
                    System.out.println(e);
                }
            };
            loader.addEventListener(listener);

            TDBRunner.BarsGenerator gn =
                    new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100, "ORCL", "WWWW");

            while (gn.next())
                loader.send(gn.getMessage());

            assertEquals(0, errors[0]);
        }

        IdentityKey[] identities = live.listEntities();
        System.out.println(Arrays.toString(identities));

        long[] range = live.getTimeRange();
        assertNotNull(range);
        System.err.println("Time range: [" + GMT.formatDateTimeMillis(range[0]) + ", " + GMT.formatDateTimeMillis(range[1]) + "]");

        int count = 0;
        try (TickCursor cursor = live.select(Long.MIN_VALUE, null)) {
            while (cursor.next())
                count++;
        }

        assertEquals(200, count);

    }

    @Test
    public void testAppend2() throws InterruptedException {
        StreamOptions options = new StreamOptions (StreamScope.DURABLE, "testAppend2", null, 0);
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        final DXTickStream live = runner.getTickDb().createStream("testAppend2", options);

        GregorianCalendar calendar = new GregorianCalendar(2016, 1, 1);
        long start = calendar.getTimeInMillis();

        LoadingOptions o = new LoadingOptions();
        o.space = "local";
        o.writeMode = LoadingOptions.WriteMode.APPEND;
        try (TickLoader loader = live.createLoader(o)) {

            final int[] errors = new int[1];
            final LoadingErrorListener listener = new LoadingErrorListener() {
                public void onError(LoadingError e) {
                    errors[0]++;
                    System.out.println(e);
                }
            };
            loader.addEventListener(listener);

            TDBRunner.BarsGenerator gn =
                    new TDBRunner.BarsGenerator(calendar, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 102, "MSFT", "IBM", "ORCL");

            while (gn.next())
                loader.send(gn.getMessage());
        }

        calendar = new GregorianCalendar(2017, 1, 1);
        long half = calendar.getTimeInMillis();

        o = new LoadingOptions();
        o.writeMode = LoadingOptions.WriteMode.APPEND;
        try (TickLoader loader = live.createLoader(o)) {

            final int[] errors = new int[1];
            final LoadingErrorListener listener = new LoadingErrorListener() {
                public void onError(LoadingError e) {
                    errors[0]++;
                    System.out.println(e);
                }
            };
            loader.addEventListener(listener);

            TDBRunner.BarsGenerator gn =
                    new TDBRunner.BarsGenerator(calendar, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 102, "ORCL", "AAPL", "MSFT");

            while (gn.next())
                loader.send(gn.getMessage());

            assertEquals(0, errors[0]);
        }

        long end = calendar.getTimeInMillis();

        IdentityKey[] identities = live.listEntities();
        assertEquals(4, identities.length);

        long[] range = live.getTimeRange(new ConstantIdentityKey("ORCL"));
        assertEquals(start, range[0]);
        assertEquals(end, range[1]);

        range = live.getTimeRange(new ConstantIdentityKey("IBM"));
        assertEquals(start, range[0]);
        assertNotEquals(end, range[1]);

        range = live.getTimeRange(new ConstantIdentityKey("AAPL"));
        assertEquals(half, range[0]);
        assertEquals(end, range[1]);
    }

    @Test
    public void testLoad() throws InterruptedException {

        StreamOptions options = new StreamOptions (StreamScope.DURABLE, "stream", null, 0);
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        final DXTickStream live = getTickDb().createStream("stream", options);
        final TickLoader loader = live.createLoader();

        Thread producer1 = new Thread() {
            @Override
            public void run() {
                TDBRunner.BarsGenerator gn1 =
                    new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 10000, "ORCL", "WWWW");

                while (gn1.next())
                    loader.send(gn1.getMessage());
            }
        };
        producer1.start();

        Thread producer2 = new Thread() {
            @Override
            public void run() {
                TDBRunner.BarsGenerator gn1 =
                    new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 10000, "MSFT", "ORCL");

                while (gn1.next())
                    loader.send(gn1.getMessage());
            }
        };
        producer2.start();

        producer1.join();
        producer2.join();

        loader.close();
    }

    public void testLoad1() throws InterruptedException {

        StreamOptions options = new StreamOptions (StreamScope.DURABLE, "stream", null, 0);

        final String            name = deltix.timebase.api.messages.BarMessage.class.getName ();

        RecordClassDescriptor descriptor = StreamConfigurationHelper.mkMarketMessageDescriptor(null);

        final DataField[]      fields = {
                StreamConfigurationHelper.mkField(
                        "exchangeCode", "Exchange Code",
                        new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                        null
                ),
                new NonStaticDataField("close", "Close", new FloatDataType(FloatDataType.ENCODING_SCALE_AUTO, true)),
                new NonStaticDataField ("open", "Open", new FloatDataType (FloatDataType.ENCODING_SCALE_AUTO, true), "close"),
                new NonStaticDataField ("high", "High", new FloatDataType (FloatDataType.ENCODING_SCALE_AUTO, true), "close"),
                new NonStaticDataField ("low", "Low", new FloatDataType (FloatDataType.ENCODING_SCALE_AUTO, true), "close"),
                new NonStaticDataField ("volume", "Volume", new FloatDataType (FloatDataType.ENCODING_SCALE_AUTO, true))
        };

        options.setFixedType(new RecordClassDescriptor (
                name, name, false,
                descriptor,
                fields
        ));

        //options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        final DXTickStream live = getTickDb().createStream("stream", options);
        final TickLoader loader = live.createLoader();

        TDBRunner.BarsGenerator gn1 =
                new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100000, "MSFT", "ORCL");

        //while (gn1.next())

        deltix.timebase.api.messages.BarMessage bar = new deltix.timebase.api.messages.BarMessage();
        bar.setSymbol("DTLX");
        bar.setTimeStampMs(TimeStamp.TIMESTAMP_UNKNOWN);

        bar.setClose(2);
        bar.setOpen(1);
        bar.setLow(-Double.MAX_VALUE);
        bar.setHigh(-Double.MAX_VALUE);

        bar.setVolume(-Double.MAX_VALUE);
        //bar.currencyCode = 840;
        loader.send(bar);

        bar = new deltix.timebase.api.messages.BarMessage();
        bar.setSymbol("DTLX1");
        bar.setTimeStampMs(TimeStamp.TIMESTAMP_UNKNOWN);

        bar.setClose(2);
        bar.setOpen(1);
        bar.setLow(3);
        bar.setHigh(3);
        loader.send(bar);

        loader.close();

//        runner.getServerDb().close();
//        runner.getServerDb().open(false);
//
//        DXTickStream stream = getTickDb().getStream("stream");
//        assert stream.getTimeRange() != null;

    }

    @Test
    @Ignore("for 4.3")
    public void testInsert() throws Throwable {
        StreamOptions options = new StreamOptions(StreamScope.DURABLE, "testTB5Insert", null, 0);
        //options.location = Home.getPath("temp/tb5insertstream/data");
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        final DXTickStream stream = runner.getServerDb().createStream("testTB5Insert", options);

        LoadingOptions lo = new LoadingOptions(false);
        lo.writeMode = LoadingOptions.WriteMode.INSERT;
        final TickLoader loader = stream.createLoader(lo);
        final TickLoader loader2 = stream.createLoader(lo);

        GregorianCalendar start5min = new GregorianCalendar();
        GregorianCalendar startMin = new GregorianCalendar();

        GregorianCalendar startTruncate = new GregorianCalendar();
        startTruncate.add(Calendar.MILLISECOND, 10 * 60 * 1000);

        final int min5Count = 10;
        final int minCount = 10;

        InstrumentMessage msg;
        TDBRunner.BarsGenerator fiveMinGen =
            new TDBRunner.BarsGenerator(start5min, (int) deltix.timebase.api.messages.BarMessage.BAR_5_MINUTE, min5Count, "DLT");
        while (fiveMinGen.next()) {
            msg = fiveMinGen.getMessage();
            loader.send(msg);
            //System.out.println(msg);
            Thread.sleep(10);
        }

        TDBRunner.BarsGenerator minGen =
            new TDBRunner.BarsGenerator(startMin, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, minCount, "DLT");

        while (minGen.next()) {
            msg = minGen.getMessage();
            //swap comment to see how logic changes
            loader.send(msg);
            //loader2.send(msg);
            //System.out.println(msg);
            Thread.sleep(10);
        }

        loader.close();
        loader2.close();

        long time = Long.MIN_VALUE;
        TickCursor cursor = stream.select(time, new SelectionOptions(true, false));

        int msgCount = 0;
        while (cursor.next()) {
            InstrumentMessage message = cursor.getMessage();
            ++msgCount;

            assertTrue("Next message time " + GMT.formatDateTimeMillis(message.getTimeStampMs()) + " < " + GMT.formatDateTimeMillis(time), message.getTimeStampMs() >= time);
            time = message.getTimeStampMs();
            //System.out.println(cursor.getMessage().toString());
        }

        assert msgCount == min5Count + minCount;
    }

    private DXTickStream createBarPDStream(String name) {
        StreamOptions options = new StreamOptions(StreamScope.DURABLE, name, null, 0);
        //options.location = Home.getPath("temp/tb5insertstream/data");
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());

        return runner.getServerDb().createStream(name, options);
    }

    private int loadWriteModeTestMessages(DXTickStream stream, LoadingOptions.WriteMode mode, long... times) {
        LoadingOptions loadingOptions = new LoadingOptions(false);
        loadingOptions.writeMode = mode;
        final TickLoader loader = stream.createLoader(loadingOptions);

        final int[] errors = new int[1];
        final LoadingErrorListener listener = new LoadingErrorListener() {
            public void onError(LoadingError e) {
                errors[0]++;
            }
        };
        loader.addEventListener(listener);

        deltix.timebase.api.messages.BarMessage msg = new deltix.timebase.api.messages.BarMessage();
        msg.setSymbol("DLT");
        for (long time : times) {
            msg.setTimeStampMs(time);
            loader.send(msg);
        }

        loader.close();

        return errors[0];
    }

    private void checkTimestamps(DXTickStream stream, long... times) {
        long time = Long.MIN_VALUE;
        int i = 0;
        TickCursor cursor = stream.select(time, new SelectionOptions(true, false));
        while (cursor.next()) {
            InstrumentMessage message = cursor.getMessage();
            if (++i > times.length)
                assert false;

            assertTrue("Timestamps not equal " + message.getTimeStampMs() + " != " + times[i - 1],
                message.getTimeStampMs() == times[i - 1]);

            //System.out.println(cursor.getMessage().toString());
        }

        assert i == times.length;
    }

    @Test
    public void testEmptyMessages() {

        StreamOptions options = new StreamOptions (StreamScope.DURABLE, "emptyBars", null, 0);
        options.setFixedType(StreamConfigurationHelper.mkBarMessageDescriptor
                (null, "NYSE", new Integer(999), FloatDataType.ENCODING_FIXED_DOUBLE, FloatDataType.ENCODING_FIXED_DOUBLE));

        final DXTickStream stream = getTickDb().createStream(options.name, options);

        try (TickLoader loader = stream.createLoader()) {
            //Random rnd = new Random(2009);
            deltix.timebase.api.messages.BarMessage message = new deltix.timebase.api.messages.BarMessage();
            message.setSymbol("DLTX");
            message.setTimeStampMs(TimeStamp.TIMESTAMP_UNKNOWN);

            loader.send(message);
        }

        try (TickCursor cursor = stream.select(0, null)) {
            while (cursor.next()) {
                System.out.println(cursor.getMessage());
            }
        }
    }

    @Test
    public void testPDStreamAppend() throws Throwable {
        DXTickStream stream = createBarPDStream("testPDStreamAppend");
        int errors = loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.APPEND, 1000, 1005, 1010);
        errors += loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.APPEND, 1002, 1006, 1015);
        errors += loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.APPEND, 1004);
        assert errors == 3;
        checkTimestamps(stream, 1000, 1005, 1010, 1015);
    }

    @Test
    public void testPDStreamTruncate() throws Throwable {
        DXTickStream stream = createBarPDStream("testPDStreamTruncate");
        loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.TRUNCATE, 1000, 1005, 1010);
        loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.TRUNCATE, 1002, 1006, 1015);
        loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.TRUNCATE, 1004);
        checkTimestamps(stream, 1000, 1002, 1004);
    }

    @Test
    public void testPDStreamRewrite() throws Throwable {
        DXTickStream stream = createBarPDStream("testPDStreamRewrite");
        loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.REWRITE, 1000, 1005, 1010);
        loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.REWRITE, 1008, 1006, 1015);
        loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.REWRITE, 1016, 1017);
        checkTimestamps(stream, 1000, 1005, 1008, 1015, 1016, 1017);
    }

    @Test
    public void testPDStreamInsert() throws Throwable {
        DXTickStream stream = createBarPDStream("testPDStreamInsert");
        if (stream.getFormatVersion() == 5) {
            loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.INSERT, 1000, 1005, 1010);
            loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.INSERT, 1006, 1002, 1015);
            loadWriteModeTestMessages(stream, LoadingOptions.WriteMode.INSERT, 1004);
            checkTimestamps(stream, 1000, 1002, 1004, 1005, 1006, 1010, 1015);
        }
    }

    public void checkTimeRange() throws InterruptedException {

        StreamOptions options = new StreamOptions (StreamScope.DURABLE, "rangeTest", null, 0);
        options.setFixedType(StreamConfigurationHelper.mkUniversalBarMessageDescriptor());
        final DXTickStream live = getTickDb().createStream("rangeTest", options);

        final TickLoader loader = live.createLoader();

        Thread producer1 = new Thread() {
            @Override
            public void run() {
                TDBRunner.BarsGenerator gn1 =
                        new TDBRunner.BarsGenerator(null, (int) deltix.timebase.api.messages.BarMessage.BAR_MINUTE, 100000, "ORCL", "WWWW");

                while (gn1.next()) {
                    loader.send(gn1.getMessage());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        producer1.start();

        //DXTickStream stream = getTickDb().getStream("rangeTest");

        while (true) {
            long[] start = live.getTimeRange();

            Thread.sleep(1000);

            long[] end = live.getTimeRange();

            if (start == null || end == null)
                continue;

            if (start[0] != end[0])
                System.out.println("Start time diff " + (end[0] - start[0]));

            if (start[1] != end[1])
                System.out.println("End time diff " + (end[1] - start[1]));
        }
    }
}
