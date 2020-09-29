using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.pub.codec;
using deltix.qsrv.hf.pub.md;
using deltix.qsrv.hf.tickdb.pub;
using deltix.util.memory;
using deltix.util.time;

namespace deltix.samples.timebase.basics {
	public class DailyBarStreamSample {
	    public static readonly string              STREAM_KEY = "daily.stream";
	
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
	            // Passing stream name, exchange code, currency code, and bar size.
	            //
	            StreamConfigurationHelper.setBar (
	            	stream, 
	            	"", 
	            	new java.lang.Integer (840),
                    deltix.util.time.Interval.DAY
	            );
	        }
	    }
	
	    public static void      loadData (DXTickDB db) {
	        DXTickStream            stream = db.getStream (STREAM_KEY);
	
	        BarMessage              bar = new BarMessage ();
	
	        TickLoader              loader = stream.createLoader ();
	        //
	        //  Always load daily bars in GMT.
	        //
	        //  The following sets the bar to "the day of 2005-01-01".
	        //
	        //  Note that the actual timestamp is exactly one day ahead of the
	        //  beginning of the bar's date in UTC.
	        //
	        DateTime				dt = DateTime.ParseExact ("2005-01-01", "yyyy-MM-dd", null).AddDays (1);
	        	        
	        for (int ii = 1; ii < 100; ii++) {
	        	bar.timestamp = DateConverter.ToLong (dt);
	            bar.instrumentType = InstrumentType.EQUITY;
	            bar.symbol = "AAPL";            
	
	            bar.open = ii;
	            bar.high = ii + .5;
	            bar.low = ii - .5;
	            bar.close = ii + .25;
	            bar.volume = 100000 + ii;
	
	            loader.send (bar);
	
	            dt = dt.AddDays (1);
	        }
	
	        loader.close();
	
	        Console.WriteLine ("Done Loading.");
	    }
	
	    public static void      readData (DXTickDB db) {
	        DXTickStream            stream = db.getStream (STREAM_KEY);
	
	        //  List of entities to subscribe (if null, all stream entities will be used)
			InstrumentIdentity[] entities = null;

			// List of types to subscribe - select 'BarMessage' only
			String[] types = new String[] { typeof(BarMessage).FullName };

			// Additional options for select()
			SelectionOptions options = new SelectionOptions();
			options.raw = false; // use decoding 
	
			//
			//  Cursor is equivalent to a JDBC ResultSet
			//
			TickCursor              cursor = stream.select (long.MinValue, options, types, entities);
		
	        BarMessage              bar;
	
	        try {
	            while (cursor.next ()) {
	                bar = (BarMessage) cursor.getMessage();
	                
					//
	                //	Convert "end of bar" timestamp to a printable date
	                //
	                Console.WriteLine (
	                	DateConverter.FromLong (bar.timestamp).AddDays (-1) + " " +
	                	bar.symbol.toString () + " " +
	                	bar.instrumentType +	                   
                        ", open: " + bar.open +
                        ", high: " + bar.high +
                        ", low: " + bar.low +
                        ", close: " + bar.close +
                        ", volume: " + bar.volume
                    );
	            }
	        } finally {
	            cursor.close ();
	        }
	        
	        Console.WriteLine ("Done Reading.");
	    }
	
	    public static void Main (string [] args) {
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
