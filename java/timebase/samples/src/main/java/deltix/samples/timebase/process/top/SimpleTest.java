package deltix.samples.timebase.process.top;

import deltix.qsrv.hf.tickdb.pub.*;
import deltix.samples.timebase.process.part.*;
import deltix.samples.timebase.process.pub.*;

/**
 *
 */
public class SimpleTest {
    public static void      main (String [] args) throws Exception {
        if (args.length == 0)
            args = new String [] { "dxtick://localhost:48011", "Intraday" };
        
        DXTickDB                db = TickDBFactory.createFromUrl (args [0]);
        
        db.open (true);
        
        try {
            PartitionBySymbol       ja = new PartitionBySymbol ();

            ja.setStreams (db.getStream (args [1]));
            
            ProcessRunner           runner = new ProcessRunner ();

            runner.setJobAllocator (ja);
            runner.setProcessorFactory (DummyMessageProcessorFactory.INSTANCE);
            runner.setProgressListener (ConsoleMultiProgressListener.INSTANCE);
            
            runner.start ();
            runner.waitUntilDone (0);
        } finally {
            db.close ();
        }
    }
}
