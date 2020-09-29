package deltix.samples.timebase.basics;

import deltix.timebase.api.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.util.time.*;

/**
 *  In order for this sample to produce something meaningful, the
 *  TimeBase instance should not be empty.
 */
public class QueryStreamsAndEntities {    
    public static void      queryStreams (DXTickDB db) {
        //
        //  Iterate over all streams
        //
        for (DXTickStream stream : db.listStreams ()) {
            System.out.printf (
                "STREAM  key: %s; name: %s; description: %s\n",
                stream.getKey (),
                stream.getName (),
                stream.getDescription ()
            );

            Periodicity    periodicity = stream.getPeriodicity ();
            
            System.out.print ("    Periodicity: ");
            
            if (periodicity.getType() != Periodicity.Type.REGULAR)
                System.out.println (periodicity.toString());
            else
                System.out.println (periodicity.getInterval().getNumUnits () + " " + periodicity.getInterval().getUnit ());

            long []     tr = stream.getTimeRange ();

            if (tr != null)
                System.out.printf ("    TIME RANGE: %tF .. %tF\n", tr [0], tr [1]);

            for (IdentityKey id: stream.listEntities ()) {
                System.out.printf (
                    "    ENTITY  type: %s; symbol: %s\n",
                    id.getInstrumentType().name (),
                    id.getSymbol ().toString ()
                );
            }
        }
    }
    
    public static void      main (String [] args) {
        if (args.length == 0)
            args = new String [] { "dxtick://localhost:8011" };

        DXTickDB    db = TickDBFactory.createFromUrl (args [0]);
        
        db.open (true);

        try {
            queryStreams (db);
        } finally {
            db.close ();
        }
    }
}
