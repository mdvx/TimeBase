package deltix.qsrv.hf.tickdb;

import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedExternalDecoder;
import deltix.qsrv.hf.pub.codec.FixedBoundEncoder;

import deltix.util.io.BasicIOUtil;
import deltix.util.io.Home;
import deltix.util.io.IOUtil;
import deltix.qsrv.hf.tickdb.util.ZIPUtil;
import deltix.util.lang.Util;
import deltix.util.memory.MemoryDataOutput;
import deltix.util.memory.MemoryDataInput;

import java.io.*;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

@Category(TickDBFast.class)
public class Test_TDBUpgrade {
    private static final File DIR = Home.getFile("/testdata/qsrv/hf/tickdb/4.2");
    private static final String ZIP = new File(DIR, "tickdb.zip").getAbsolutePath();
    private static final String ZIP_WITH_DATA = new File(DIR, "tickdb.data.zip").getAbsolutePath();
    private static final String ZIP_WITH_DATA_UPGRADE = new File(DIR, "tickdb.convert.zip").getAbsolutePath();
    private static final String ZIP_WITH_DATA_REDSKY = new File(DIR, "tickdb.redsky.zip").getAbsolutePath();
    private static final String EXCH_CODES = new File(DIR, "exch_codes.csv").getAbsolutePath();

    private final static String       LOCATION = TDBRunner.getTemporaryLocation();

    @Test
    public void Test_Convert() throws IOException, InterruptedException {
        createDB(ZIP);
        DXTickDB tickDB = TickDBFactory.create(LOCATION);
        tickDB.open(false);
        assertEquals(7, tickDB.listStreams().length);
        tickDB.close();
    }

    /* When 4.2 is opened by 4.3, the default conversion occurs.
     * It converts exchangeCode (byte) to a dummy field.
     *
     * Test that bounded decoder and encoder handle a dummy field correctly.
     */
    @Test
    @Ignore("new message format")
    public void testDummyFieldSkip() throws IOException, InterruptedException {
        final TickDBUtil util = new TickDBUtil();
        createDB(ZIP_WITH_DATA);
        DXTickDB tickDB = TickDBFactory.create(LOCATION);
        tickDB.open(false);
        try {
            assertEquals(2, tickDB.listStreams().length);
            DXTickStream stream = tickDB.getStream("ticks");
            LoadingOptions options = new LoadingOptions();
            options.writeMode = LoadingOptions.WriteMode.REPLACE;
                        
            // test loading
            TickLoader loader = stream.createLoader(options);
            try {
                // O,EQUITY,ORCL,11/13/2008 19:42:57.103,,,,9.52,1900,83,9.54,1401
                final deltix.timebase.api.messages.BestBidOfferMessage bbo = new deltix.timebase.api.messages.BestBidOfferMessage();
                bbo.setSymbol("ORCL");
                bbo.setTimeStampMs(1226605377103L);
                bbo.setBidPrice(9.52);
                bbo.setBidSize(1900);
                bbo.setOfferPrice(9.54);
                bbo.setOfferSize(1401);
                loader.send(bbo);

                // T,EQUITY,ORCL,11/13/2008 19:42:58.595,9.54,101
                final deltix.timebase.api.messages.TradeMessage trade = new deltix.timebase.api.messages.TradeMessage();
                trade.setSymbol("AAPL");
                trade.setTimeStampMs(1226605378595L);
                trade.setPrice(9.54);
                trade.setSize(101);
                loader.send(trade);
            } finally {
                loader.close();
            }


            TickCursor cursor = TickCursorFactory.create(stream, 0);
            final String output = util.toString(cursor);
            Util.close(cursor);

            //final String output = util.select(0, stream, null, false);
            //TickDBUtil.dump2File(TickDBUtil.USER_HOME + "upgrade.txt", output);
            final String etalon = IOUtil.readTextFile (new File (DIR, "upgrade.txt"));
            Assert.assertEquals("Data log is not the same as etalon", etalon, output);

        } finally {
            tickDB.close();
        }
    }

    @Test
    @Ignore("new message format")
    public void testDummyFieldSkipIntepreted() throws IOException, InterruptedException {
        createDB(ZIP_WITH_DATA);
        DXTickDB tickDB = TickDBFactory.create(LOCATION);
        tickDB.open(false);
        try {
            assertEquals(2, tickDB.listStreams().length);
            DXTickStream stream = tickDB.getStream("ticks");
            
            final RecordClassDescriptor rcdTrade = stream.getPolymorphicDescriptors()[0];
            final RecordClassDescriptor rcdBBO = stream.getPolymorphicDescriptors()[1];
            final deltix.timebase.api.messages.TradeMessage trade = new deltix.timebase.api.messages.TradeMessage();
            final deltix.timebase.api.messages.BestBidOfferMessage bbo = new deltix.timebase.api.messages.BestBidOfferMessage();

            LoadingOptions options = new LoadingOptions(true);
            options.writeMode = LoadingOptions.WriteMode.REPLACE;

            // test loading
            TickLoader loader = stream.createLoader(options);

            try {
                final RawMessage raw = new RawMessage();

                // O,EQUITY,ORCL,11/13/2008 19:42:57.103,,,,9.52,1900,83,9.54,1401
                raw.setSymbol("ORCL");
                raw.setTimeStampMs(1226605377103L);
                raw.type = rcdBBO;

                bbo.setBidPrice(9.52);
                bbo.setBidSize(1900);
                bbo.setOfferPrice(9.54);
                bbo.setOfferSize(1401);
                
                FixedBoundEncoder encoder = CodecFactory.INTERPRETED.createFixedBoundEncoder(TypeLoaderImpl.DEFAULT_INSTANCE, raw.type);
                MemoryDataOutput out = new MemoryDataOutput(); 
                encoder.encode(bbo, out);
                raw.setBytes(out, 0);
                loader.send(raw);

                raw.setSymbol("AAPL");
                raw.setTimeStampMs(1226605378595L);
                raw.type = rcdTrade;

                // T,EQUITY,ORCL,11/13/2008 19:42:58.595,9.54,101
                trade.setPrice(9.54);
                trade.setSize(101);
                
                encoder = CodecFactory.INTERPRETED.createFixedBoundEncoder(TypeLoaderImpl.DEFAULT_INSTANCE, raw.type);
                out.reset();
                encoder.encode(trade, out);
                raw.setBytes(out, 0);
                loader.send(raw);
            } finally {
                loader.close();
            }


            final TickDBUtil util = new TickDBUtil();
            final StringBuilder out = new StringBuilder();
            final TickCursor cursor = stream.select(0, new SelectionOptions(true, false));
            final FixedExternalDecoder decoderTrade = CodecFactory.INTERPRETED.createFixedExternalDecoder(TypeLoaderImpl.DEFAULT_INSTANCE, rcdTrade);
            final FixedExternalDecoder decoderBBO = CodecFactory.INTERPRETED.createFixedExternalDecoder(TypeLoaderImpl.DEFAULT_INSTANCE, rcdBBO);
            decoderBBO.setStaticFields(bbo);
            
            final MemoryDataInput in = new MemoryDataInput();
            while (cursor.next()) {
                RawMessage raw = (RawMessage) cursor.getMessage();
                if(raw.type == rcdTrade) {
                    trade.setSymbol(raw.getSymbol());
                    trade.setTimeStampMs(raw.getTimeStampMs());
                    raw.setUpMemoryDataInput(in);
                    decoderTrade.decode(in, trade);
                    util.print(trade, out);
                }
                else if(raw.type == rcdBBO) {
                    bbo.setSymbol(raw.getSymbol());
                    bbo.setTimeStampMs(raw.getTimeStampMs());
                    raw.setUpMemoryDataInput(in);
                    decoderBBO.decode(in, bbo);
                    util.print(bbo, out);
                }
                else
                    throw new IllegalStateException(raw.type.toString());
            }

            //TickDBUtil.dump2File(TickDBUtil.USER_HOME + "upgrade.txt", output);
            final String[] lines = IOUtil.readLinesFromTextFile (new File (DIR, "upgrade.txt"));
            String etalon = String.join(System.lineSeparator(), lines);

            Assert.assertEquals("Data log is not the same as etalon", etalon, out.toString());

        } finally {
            tickDB.close();
        }
    }

    private static final String[] STREAM_NAMES = {
        "ticks.ns",     // bbo+trade: all exchangeCodes non-static
        "ticks.st",     // bbo+trade: all exchangeCodes static
        "trades.ns",    // trades: all exchangeCodes non-static
        "trades.st",    // trades: all exchangeCodes static
        "bbo.bug.6538", // bbo: exchangeCode static UHF8 
        "bars.ns",      // bars: exchangeCode + barSize non-static
        "bars.st"       // bars: exchangeCode + barSize static
    };

    /*
        Test Automatic Upgrade 4.2 - 4.3. Selection from streams ensures that conversion was correct.
     */
    @Test
    @Ignore("new message format")
    public void testAutoUpgrade() throws IOException, InterruptedException {
        createDB(ZIP_WITH_DATA_UPGRADE);
        DXTickDB tickDB = TickDBFactory.create(LOCATION);
        tickDB.open(false);

        try {
            assertEquals(STREAM_NAMES.length + 1, tickDB.listStreams().length);
            final TickDBUtil util = new TickDBUtil();

            for (String streamName : STREAM_NAMES) {
                final DXTickStream stream = tickDB.getStream(streamName);

                //String output = util.select(0, stream, null, false);
                TickCursor cursor = TickCursorFactory.create(stream, 0);
                final String output = util.toString(cursor);
                Util.close(cursor);

                String fileName = "upgrade_" + streamName.substring(0, streamName.lastIndexOf('.')) + ".txt";
                //TickDBUtil.dump2File(TickDBUtil.USER_HOME + fileName, output);
                String etalon = IOUtil.readTextFile (new File (DIR, fileName));
                Assert.assertEquals("Discrepancy in stream " + streamName + " (template " + fileName + "):", etalon, output);

                cursor = TickCursorFactory.create(stream, 0, new SelectionOptions(true, false));
                final String output2 = util.toString(cursor);
                Util.close(cursor);

                //String output2 = util.select(0, stream, null, true, false);
                fileName = "upgrade_" + streamName + ".raw.txt";
                //TickDBUtil.dump2File(TickDBUtil.USER_HOME + fileName, output);
                etalon = IOUtil.readTextFile (new File (DIR, fileName));

                Assert.assertEquals("Discrepancy in stream " + streamName + " (template " + fileName + "):", etalon, output2);
            }
        } finally {
            tickDB.close();
        }
    }

    private static final String[] BOUND_ETALON_NAMES = {
        "upgrade_ticks.exch.txt",
        "upgrade_ticks.txt",
        "upgrade_trades.exch.txt",
        "upgrade_trades.txt",
        "upgrade_bbo.bug.6538.exch.txt",
        "upgrade_bars.exch.txt",
        "upgrade_bars.txt"
    };

    private static final String[] RAW_ETALON_NAMES = {
        "upgrade_ticks.ns.raw.exch.txt",
        "upgrade_ticks.st.raw.txt",
        "upgrade_trades.ns.raw.exch.txt",
        "upgrade_trades.st.raw.txt",
        "upgrade_bbo.bug.6538.raw.exch.txt",
        "upgrade_bars.ns.raw.exch.txt",
        "upgrade_bars.st.raw.txt"
    };

    

    private static void createDB(String zipFileName) throws IOException, InterruptedException {
        File folder = new File(LOCATION);
        BasicIOUtil.deleteFileOrDir(folder);
        folder.mkdirs();

        FileInputStream is = new FileInputStream(zipFileName);
        ZIPUtil.extractZipStream(is, folder);
        is.close();
    }
}