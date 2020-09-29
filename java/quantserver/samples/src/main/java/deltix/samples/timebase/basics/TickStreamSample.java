package deltix.samples.timebase.basics;

import deltix.timebase.api.messages.IdentityKey;
import deltix.timebase.api.messages.InstrumentMessage;

import deltix.qsrv.hf.tickdb.pub.*;
import deltix.util.time.GMT;

import java.text.ParseException;

public class TickStreamSample {
    public static final String              STREAM_KEY = "tick.stream";

    /**
     *  Instead of programmatic creation illustrated below, the user will
     *  more likely create the stream using TimeBase Administrator.
     */
    public static void      createSampleStream (DXTickDB db) {
        DXTickStream            stream = db.getStream (STREAM_KEY);

        if (stream == null) {
            stream =
                db.createStream (
                    STREAM_KEY,
                    STREAM_KEY,
                    "Description Line1\nLine 2\nLine 3",
                    0
                );
            //
            // Passing stream name, national level, exchange code, currency code
            //
            StreamConfigurationHelper.setTradeOrBBO (stream, true, "XNYS", 840);
        }
    }

    public static void      loadData (DXTickDB db) throws ParseException {
        DXTickStream            stream = db.getStream (STREAM_KEY);
        //
        //  Create reusable message objects.
        //
        final deltix.timebase.api.messages.TradeMessage tradeMessage = new deltix.timebase.api.messages.TradeMessage();
        final deltix.timebase.api.messages.BestBidOfferMessage bboMessage = new deltix.timebase.api.messages.BestBidOfferMessage();
        //
        //  Initialize fields that will not change inside the loop.
        //
        bbo
        tradeMessage.setInstrumentType(bboMessage.getInstrumentType());

        bboMessage.setSymbol("IBM");
        tradeMessage.setSymbol(bboMessage.getSymbol());

        final TickLoader        loader = stream.createLoader ();
        final long              time = System.currentTimeMillis ();
        //
        //  In this example, generate 10 "ticks" per second 
        //  for 10 minutes, beginning with current system time.
        //
        for (int ii = 0; ii < 6000; ii++) {
            double              simulatedPrice = 126 + ii / 200.0;
            double              simulatedSize = (ii % 10) * 100;
            long                simulatedTimestamp = time + ii * 100; // Every 100ms

            switch (ii % 4) {
                case 0:
                    //  Generate a trade tick
                    tradeMessage.setTimeStampMs(simulatedTimestamp);
                    tradeMessage.setPrice(simulatedPrice);
                    tradeMessage.setSize(simulatedSize);

                    loader.send (tradeMessage);
                    break;

                case 1:
                    //  Generate a two-sided quote
                    bboMessage.setTimeStampMs(simulatedTimestamp);
                    bboMessage.setBidPrice(simulatedPrice - 0.01);
                    bboMessage.setBidSize(simulatedSize * 5);
                    bboMessage.setOfferPrice(simulatedPrice + 0.01);
                    bboMessage.setOfferSize(simulatedSize * 4);

                    loader.send (bboMessage);
                    break;

                case 2:
                    //  Generate a one-sided bid quote
                    bboMessage.setTimeStampMs(simulatedTimestamp);
                    bboMessage.setBidPrice(simulatedPrice - 0.01);
                    bboMessage.setBidSize(simulatedSize * 5);
                    bboMessage.setOfferPrice(Double.NaN);         // Must null out
                    bboMessage.setOfferSize(Double.NaN);          // Must null out

                    loader.send (bboMessage);
                    break;

                case 3:
                    //  Generate a one-sided offer quote
                    bboMessage.setTimeStampMs(simulatedTimestamp);
                    bboMessage.setBidPrice(Double.NaN);           // Must null out
                    bboMessage.setBidSize(Double.NaN);            // Must null out
                    bboMessage.setOfferPrice(simulatedPrice + 0.01);
                    bboMessage.setOfferSize(simulatedSize * 5);

                    loader.send (bboMessage);
                    break;
            }           
        }

        loader.close ();

        System.out.println ("Done.");
    }

    public static void      readData (DXTickDB db) {
        DXTickStream            stream = db.getStream (STREAM_KEY);
                
        //
        //  List of entities to subscribe (if null, all stream entities will be used)
        //
        IdentityKey[] entities = null;

        //
        //  List of types to subscribe - select trades and bbo's only
        //
        String[] types = new String[] {
                deltix.timebase.api.messages.TradeMessage.class.getName(),
                deltix.timebase.api.messages.BestBidOfferMessage.class.getName()
        };

        //
        //  Additional options for select()
        //
        SelectionOptions options = new SelectionOptions();
        options.raw = false; // use decoding
        
        //
        //  Cursor is equivalent to a JDBC ResultSet
        //
        TickCursor              cursor = stream.select (Long.MIN_VALUE, options, types, entities);

        try {
            while (cursor.next ()) {
                InstrumentMessage msg = cursor.getMessage ();

                System.out.print (
                    "symbol: " + msg.getSymbol() +
                    "; instrumentType: " + msg.getInstrumentType() +
                    "; timestamp: " + GMT.formatDateTimeMillis (msg.getTimeStampMs()) +
                    " (GMT)\n    "
                );

                if (msg instanceof deltix.timebase.api.messages.TradeMessage) {
                    deltix.timebase.api.messages.TradeMessage trade = (deltix.timebase.api.messages.TradeMessage) msg;

                    System.out.println (
                        "price: " + trade.getPrice() +
                        "; size: " + trade.getSize()
                    );
                }
                else if (msg instanceof deltix.timebase.api.messages.BestBidOfferMessage) {
                    deltix.timebase.api.messages.BestBidOfferMessage bbo = (deltix.timebase.api.messages.BestBidOfferMessage) msg;

                    System.out.println (
                        "bidPrice: " + bbo.getBidPrice() +
                        "; bidSize: " + bbo.getBidSize() +
                        "; offerPrice: " + bbo.getOfferPrice() +
                        "; offerSize: " + bbo.getOfferSize()
                    );
                }
                else
                    throw new RuntimeException ("Unrecognized message type: " + msg);
            }
        } finally {
            cursor.close ();
        }

        System.out.println ("Done.");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            args = new String [] { "dxtick://localhost:8011" };

        DXTickDB    db = TickDBFactory.createFromUrl (args [0]);

        db.open (false);

        try {
            createSampleStream (db);
            loadData (db);            
            readData (db);
        } finally {
            db.close ();
        }
    }
}
