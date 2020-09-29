using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.tickdb.pub;
using deltix.util.time;

namespace deltix.samples.timebase.basics {
	/// <summary>
	/// In order for this sample to produce something meaningful, the
	/// TimeBase instance should not be empty.
	/// </summary>
    public class QueryStreamsAndEntities {    
        public static void      queryStreams (DXTickDB db) {
            //
            //  Iterate over all streams
            //
            foreach (DXTickStream stream  in  db.listStreams ()) {
                Console.WriteLine (
            		"STREAM  key: " + stream.getKey () +
            		"; name: " + stream.getName () + 
            		"; description: " + 
            		stream.getDescription ()
                );

                Interval    periodicity = stream.getPeriodicity ().getInterval();

                Console.Write ("    Periodicity: ");

                if (periodicity == null)
                    Console.WriteLine ("Irregular");
                else
                    Console.WriteLine (periodicity.getNumUnits () + " " + periodicity.getUnit ());

                long []     tr = stream.getTimeRange ();

                if (tr != null)
                    Console.WriteLine (
                	"    TIME RANGE: " +
                	DateConverter.FromLong (tr [0]) + " .. " + DateConverter.FromLong (tr [1])
                    );

                foreach (InstrumentIdentity id in  stream.listEntities ()) {
                    Console.WriteLine (
                        "    ENTITY  type: " + id.getType ().name () +
                        "; symbol: " + id.getSymbol ().toString ()
                    );
                }
            }
        }

        public static void Main (string [] args) {
            if (args.Length == 0)
                args = new string [] { "dxtick://localhost:8011" };

            DXTickDB    db = TickDBFactory.createFromUrl (args [0]);

            db.open (true);

            try {
                queryStreams (db);
            } finally {
                db.close ();
            }
        }
    }
}
