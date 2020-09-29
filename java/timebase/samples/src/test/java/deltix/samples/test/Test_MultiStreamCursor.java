package deltix.samples.test;


import deltix.qsrv.hf.pub.md.Introspector;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.*;

import deltix.timebase.api.messages.BestBidOfferMessage;
import deltix.timebase.api.messages.InstrumentType;
import deltix.timebase.api.messages.MarketMessage;
import deltix.timebase.api.messages.TradeMessage;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.lang.Util;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** 
 *  Test that we can independently load information into the same stream from
 *  multiple loaders, as long as loaders do not overlap on symbols.
 */
public class Test_MultiStreamCursor extends TDBTestBase {
    public static final int         NUM_MESSAGES = 10000;
    public static final String      SYMBOL = "DLTX";
    public static final int         TRADE_INTERVAL = 10;

    public void         createDB (DXTickDB db) throws Introspector.IntrospectionException {

        deleteIfExists("trades");
        RecordClassDescriptor mk = (RecordClassDescriptor) Introspector.introspectSingleClass(TradeMessage.class);
        StreamOptions options = StreamOptions.fixedType (StreamScope.DURABLE, null, null, 1, mk);
        DXTickStream            ts = db.createStream ("trades", options);
        deleteIfExists("bbos");
        RecordClassDescriptor bboDes = (RecordClassDescriptor) Introspector.introspectSingleClass(BestBidOfferMessage.class);
        StreamOptions optionsBbo = StreamOptions.fixedType (StreamScope.DURABLE, null, null, 1, bboDes);
        DXTickStream            bbos = db.createStream ("bbos", optionsBbo);

        //StreamConfigurationHelper.setTradeNoExchNoCur (ts);
        //StreamConfigurationHelper.setBBONoExchNoCur (bbos);

        deltix.timebase.api.messages.TradeMessage trade = new deltix.timebase.api.messages.TradeMessage();
        deltix.timebase.api.messages.BestBidOfferMessage bbo = new deltix.timebase.api.messages.BestBidOfferMessage();

        bbo.setSymbol(SYMBOL);
        trade.setSymbol(SYMBOL);

        TickLoader              tl = ts.createLoader ();
        TickLoader              bbol = bbos.createLoader ();
        
        for (int ii = 0; ii < NUM_MESSAGES; ii++) {
            if (ii % TRADE_INTERVAL == (TRADE_INTERVAL - 1)) {
                trade.setTimeStampMs(ii);
                trade.setSize(ii);
                trade.setPrice(ii);

                tl.send (trade);
            }
            else {
                bbo.setTimeStampMs(ii);
                bbo.setBidPrice(ii);
                bbo.setBidSize(ii);
                bbo.setOfferPrice(ii);
                bbo.setOfferSize(ii);

                bbol.send (bbo);
            }
        }

        tl.close ();
        bbol.close ();
    }

    private void        checkLoad (DXTickDB db) {
        try (TickCursor          cur =
            db.select (0, null, null, (CharSequence[]) null, db.getStream("trades"), db.getStream("bbos"))) {

            for (int ii = 0; ii < NUM_MESSAGES; ii++) {
                assertTrue("Failed to get message #" + ii, cur.next());

                InstrumentMessage msg = cur.getMessage();

                assertEquals(ii, msg.getTimeStampMs());
                assertTrue(Util.equals(SYMBOL, msg.getSymbol()));

                if (ii % TRADE_INTERVAL == (TRADE_INTERVAL - 1)) {
                    deltix.timebase.api.messages.TradeMessage trade = (deltix.timebase.api.messages.TradeMessage) msg;

                    assertEquals(ii, (int) trade.getSize());
                    assertEquals(ii, (int) trade.getPrice());
                } else {
                    deltix.timebase.api.messages.BestBidOfferMessage bbo = (deltix.timebase.api.messages.BestBidOfferMessage) msg;

                    assertEquals(ii, (int) bbo.getBidPrice());
                    assertEquals(ii, (int) bbo.getBidSize());
                    assertEquals(ii, (int) bbo.getOfferPrice());
                    assertEquals(ii, (int) bbo.getOfferSize());
                }
            }
        }
    }
    
    @Test
    public void         test () throws Introspector.IntrospectionException {
        createDB(getTickDb());
        checkLoad (getTickDb());
    }
}
