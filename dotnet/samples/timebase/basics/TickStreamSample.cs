using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.tickdb.pub;
using deltix.util.time;

using java.text;

namespace deltix.samples.timebase.basics {
    public class TickStreamSample {
        public static readonly string              STREAM_KEY = "tick.stream";

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
                StreamConfigurationHelper.setTradeOrBBO (stream, new java.lang.Boolean (true), "XNYS", new java.lang.Integer (840));
            }
        }

        public static void      loadData (DXTickDB db) {
            DXTickStream            stream = db.getStream (STREAM_KEY);
            //
            //  Create reusable message objects.
            //
            TradeMessage        tradeMessage = new TradeMessage ();
            BestBidOfferMessage bboMessage = new BestBidOfferMessage ();
            //
            //  Initialize fields that will not change inside the loop.
            //
            tradeMessage.instrumentType = bboMessage.instrumentType =
                    InstrumentType.EQUITY;

            tradeMessage.symbol = bboMessage.symbol = "IBM";

            TickLoader        loader = stream.createLoader ();
            long              time = DateConverter.ToLong (DateTime.Now);
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
                        tradeMessage.timestamp = simulatedTimestamp;
                        tradeMessage.price = simulatedPrice;
                        tradeMessage.size = simulatedSize;

                        loader.send (tradeMessage);
                        break;

                    case 1:
                        //  Generate a two-sided quote
                        bboMessage.timestamp = simulatedTimestamp;
                        bboMessage.bidPrice = simulatedPrice - 0.01;
                        bboMessage.bidSize = simulatedSize * 5;
                        bboMessage.offerPrice = simulatedPrice + 0.01;
                        bboMessage.offerSize = simulatedSize * 4;

                        loader.send (bboMessage);
                        break;

                    case 2:
                        //  Generate a one-sided bid quote
                        bboMessage.timestamp = simulatedTimestamp;
                        bboMessage.bidPrice = simulatedPrice - 0.01;
                        bboMessage.bidSize = simulatedSize * 5;
                        bboMessage.offerPrice = Double.NaN;         // Must null out
                        bboMessage.offerSize = Double.NaN;          // Must null out

                        loader.send (bboMessage);
                        break;

                    case 3:
                        //  Generate a one-sided offer quote
                        bboMessage.timestamp = simulatedTimestamp;
                        bboMessage.bidPrice = Double.NaN;           // Must null out
                        bboMessage.bidSize = Double.NaN;            // Must null out
                        bboMessage.offerPrice = simulatedPrice + 0.01;
                        bboMessage.offerSize = simulatedSize * 5;

                        loader.send (bboMessage);
                        break;
                }           
            }

            loader.close ();

            Console.WriteLine ("Done.");
        }

        public static void      readData (DXTickDB db) {
            DXTickStream            stream = db.getStream (STREAM_KEY);
            //
			//  List of entities to subscribe (if null, all stream entities will be used)
			//
			InstrumentIdentity[] entities = null;

			//
			//  List of types to subscribe - select trades and bbo's only
			//
			String[] types = new String[] {
					typeof(TradeMessage).FullName,
					typeof(BestBidOfferMessage).FullName
			};

			//
			//  Additional options for select()
			//
			SelectionOptions options = new SelectionOptions();
			options.raw = false; // use decoding
		
            //
            //  Cursor is equivalent to a JDBC ResultSet
            //
            TickCursor              cursor = stream.select (long.MinValue, options, types, entities);

            try {
                while (cursor.next ()) {
                    InstrumentMessage   msg = cursor.getMessage ();

                    Console.Write (
                    	"symbol: " + msg.symbol.toString () +
                        "; instrumentType: " + msg.instrumentType +
                        "; timestamp: " + GMT.formatDateTimeMillis (msg.timestamp) +
                        " (GMT)\n    "
                    );

                    if (msg is TradeMessage) {
                        TradeMessage    trade = (TradeMessage) msg;

                        Console.WriteLine (
                            "price: " + trade.price +
                            "; size: " + trade.size
                        );
                    }
                    else if (msg is BestBidOfferMessage) {
                        BestBidOfferMessage bbo = (BestBidOfferMessage) msg;

                        Console.WriteLine (
                            "bidPrice: " + bbo.bidPrice +
                            "; bidSize: " + bbo.bidSize +
                            "; offerPrice: " + bbo.offerPrice +
                            "; offerSize: " + bbo.offerSize
                        );
                    }
                    else
                        throw new Exception ("Unrecognized message type: " + msg);
                }
            } finally {
                cursor.close ();
            }

            Console.WriteLine ("Done.");
        }

        public static void Main(string[] args) {
            if (args.Length == 0)
                args = new string [] { "dxtick://localhost:8011" };

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
}
