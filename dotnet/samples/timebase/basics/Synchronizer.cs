using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.tickdb.pub;
using deltix.qsrv.hf.tickdb.pub.query;
using deltix.util.time;
using java.util;

namespace deltix.samples.timebase.basics {
	/// <summary>
	/// This example shows how to synchronize data in two streams. For convenience,
	///  it also auto-creates the stream and has a built-in test fixture
	/// that adds more data between synchronization sessions.
	/// <p>
	/// The following limitations must be understood:
	/// <ul>
	/// <li>This code does not account for the possibility of any additional mutation of
	///     the target stream, or for any retroactive corrections in the source
	///     stream. We assume that for every symbol the last known timestamp in
	///     the target stream is the only necessary consideration for
	///     synchronization.</li>
	/// <li>This code may not work correctly if the source stream is still being
	///     added to while synchronization is underway.</li>
	/// <li>This code will not work with custom types.</li>
	/// </ul>
	/// </p>
	/// </summary>
    public class Synchronizer {
        private static long             timestampForGeneratedData =
        	DateConverter.ToLong (DateTime.Now) / 1000 * 1000; // truncate milliseconds

        public static DXTickStream      createSampleStream (DXTickDB db, string key, int df) {
            //
            //  Create a one-second bar stream
            //
            DXTickStream            stream = db.getStream (key);

            if (stream != null)
                stream.delete ();

            stream =
                db.createStream (
                    key,
                    key,
                    "Test 1-second bar stream for\nthe Synchronizer sample.",
                    df
                );
            //
            // Passing stream name, exchange code, currency code, and bar size.
            //
            StreamConfigurationHelper.setBar (stream, "XNYS", new java.lang.Integer (840), Interval.SECOND);

            return (stream);
        }

        public static void      generateTestData (
            DXTickStream            stream,
            params string []        symbols
        )
        {
            //  Create a reusable BarMessage object
            BarMessage              bar = new BarMessage ();

            bar.instrumentType = InstrumentType.EQUITY;

            TickLoader              loader = stream.createLoader ();

            //  Generate 60 messages per symbol
            for (int ii = 0; ii < 60; ii++) {
                bar.timestamp = timestampForGeneratedData;

                foreach (string s  in  symbols) {
                    bar.symbol = s;
                    //
                    //  Fill with fairly meaningless data; this is not essential to
                    //  the synchronization issue being illustrated.
                    //
                    bar.open = ii + 10;
                    bar.close = ii + 11;
                    bar.high = ii + 12;
                    bar.low = ii + 9;
                    bar.volume = ii + 1;

                    loader.send (bar);
                }

                timestampForGeneratedData += 1000;
            }

            loader.close ();        
        }

        public static void      printDimensions (DXTickStream s) {
            Console.WriteLine ("Dimensions of stream " + s.getName () + ": [");

            InstrumentIdentity []   ids = s.listEntities ();

            Arrays.sort (ids);

            foreach (InstrumentIdentity id  in  ids) {
                long []             tr = s.getTimeRange (id);

                Console.Write (" " + id + ": ");

                if (tr == null)
                    Console.WriteLine ("NO DATA");
                else
                    Console.WriteLine (
                        GMT.formatDateTime (tr [0]) + " .. " +
                        GMT.formatDateTime (tr [1])
                    );
            }

            Console.WriteLine ("]");
        }

        public static void      synchronize (DXTickStream source, DXTickStream target) {
            long            globalStartTime = long.MaxValue;

            foreach (InstrumentIdentity id  in  source.listEntities ()) {
                long []     sourceRange = source.getTimeRange (id);

                if (sourceRange == null) {
                    //
                    //  No data in source. Can happen in real life,
                    //  but should not happen in this test
                    //
                    Console.WriteLine ("No data in source stream for " + id + "; skipping...");
                    continue;
                }

                long []     targetRange = target.getTimeRange (id);

                if (targetRange == null) {
                    //
                    //  Target stream has no data for this entity.
                    //
                    if (globalStartTime > sourceRange [0])
                        globalStartTime = sourceRange [0];
                }
                else {
                    //
                    //  Give a warning if source time ranges do not match.
                    //
                    if (targetRange [0] != sourceRange [0]) 
                        Console.WriteLine (
                            "Warning: " + id + " has source data starting at " +
                            GMT.formatDateTime (sourceRange [0]) +
                            ",\n    but target data starting at " +
                            GMT.formatDateTime (targetRange [0]) +
                            ".\n    This discrepancy is reported, but ignored."
                        );
                    //
                    //  While this is in no way a complete consistency check, 
                    //  at least check that target does not have data that is
                    //  LATER than source.
                    //
                    if (targetRange [1] > sourceRange [1]) {
                        Console.WriteLine (
                            "Error: " + id + " has target data ending at " +
                            GMT.formatDateTime (targetRange [1]) +
                            ",\n    which is LATER than source data ending at " +
                            GMT.formatDateTime (sourceRange [1]) +
                            ".\n    Synchronization of this symbol is aborted."
                        );

                        continue;
                    }

                    if (globalStartTime > targetRange [1])
                        globalStartTime = targetRange [1];
                }
            }
            //
            //  Select all source data beginning at globalStartTime
            //  and load it into target.
            //
            InstrumentMessageSource     cur = null;
            TickLoader                  loader = null;

            try {
                cur = source.createCursor (null);
                loader = target.createLoader ();
                //
                //  Download the increment.
                //
                cur.reset (globalStartTime);
                cur.subscribeToAllEntities ();
                //
                //  The actual copying is trivial, as follows:
                //
                while (cur.next ())
                    loader.send (cur.getMessage ());
            } finally {
                if (cur != null)
                    cur.close ();

                if (loader != null)
                    loader.close ();
            }
        }

        public static void Main (string [] args) {
            string      sourceUrl;
            string      targetUrl;
            string      sourceKey = "synchronizer.source";
            string      targetKey = "synchronizer.target";
            //
            //  A few different ways to send command arguments...
            //
            switch (args.Length) {
                case 0:
                    sourceUrl = targetUrl = "dxtick://localhost:8011";
                    break;

                case 1:
                    sourceUrl = targetUrl = args [0];
                    break;

               case 2:
                    sourceUrl = args [0];
                    targetUrl = args [1];
                    break;

                case 3:
                    sourceUrl = targetUrl = args [0];
                    sourceKey = args [1];
                    targetKey = args [2];
                    break;

                case 4:
                    sourceUrl = args [0];
                    targetUrl = args [1];
                    sourceKey = args [2];
                    targetKey = args [3];
                    break;

                default:
                    throw new Exception ("Too many arguments");
            }

            DXTickDB    sourceDb = TickDBFactory.createFromUrl (sourceUrl);
            DXTickDB    targetDb = TickDBFactory.createFromUrl (targetUrl);
            //
            //  Opening source as Read-Write because we will be generating test data.
            //  For pure synchronization, it should be opened as Read-Only.
            //
            sourceDb.open (false);
            targetDb.open (false);

            try {
                //
                //  Test preparation. Create two identical streams with DF=MAX.
                //
                DXTickStream    sourceStream = 
                    createSampleStream (sourceDb, sourceKey, StreamOptions.MAX_DISTRIBUTION);

                DXTickStream    targetStream =
                    createSampleStream (targetDb, targetKey, StreamOptions.MAX_DISTRIBUTION);
                //
                //  Add some data to source stream and print it out
                //
                generateTestData (sourceStream, "ORCL", "GOOG");
                //
                //  Print test data
                //
                printDimensions (sourceStream);
                //
                //  Finally! We are ready to synchronize data.
                //
                synchronize (sourceStream, targetStream);
                //
                //  Now we should see the test data - synchronized.
                //
                printDimensions (targetStream);
                //
                //  Add some more test data, including new symbols and some old
                //
                generateTestData (sourceStream, "ORCL", "IBM", "AAPL");
                //
                //  Print test data again
                //
                printDimensions (sourceStream);
                //
                //  Finally! We are ready to synchronize data.
                //
                synchronize (sourceStream, targetStream);
                //
                //  Now we should see the test data - synchronized.
                //
                printDimensions (targetStream);

            } finally {
                targetDb.close ();
                sourceDb.close ();
            }
        }
    }
}
