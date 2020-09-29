package deltix.samples.timebase.process.top;

import deltix.qsrv.hf.tickdb.comm.client.TickDBClient;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.samples.timebase.process.demos.IntradayDataGapFinder;
import deltix.samples.timebase.process.part.PartitionBySymbol;
import deltix.samples.timebase.process.pub.ConfigurableMessageProcessorFactory;
import deltix.samples.timebase.process.pub.ProcessRunner;
import deltix.samples.timebase.process.ui.DemoFrame;

import javax.swing.*;

/**
 *  Runs the IntradayDataGapFinder processor in the GUI framework.
 */
public class GapFinderGUITest {
    public static void      main (String [] args) throws Exception {
        String                  host = "localhost";
        int                     port = 8011;
        String                  streamKey = "Ticks";
        //*********************************************************************
        //  Open a TimeBase connection
        //*********************************************************************
        DXTickDB                db = new TickDBClient (host, port);
        
        db.open (true);
        
        DXTickStream            stream = db.getStream (streamKey);  
        
        if (stream == null)
            throw new RuntimeException ("No such stream");
        
        ProcessRunner           runner = new ProcessRunner ();
        //*********************************************************************
        //  MAIN INIT PART 1: Initialize the job allocator
        //*********************************************************************
        PartitionBySymbol       ja = new PartitionBySymbol ();

        ja.setStreams (stream);           
        
        runner.setJobAllocator (ja);
        //*********************************************************************
        //  MAIN INIT PART 2: Initialize the message processor
        //*********************************************************************
        ConfigurableMessageProcessorFactory factory = 
            new ConfigurableMessageProcessorFactory ();
        
        factory.setProcessorClass (IntradayDataGapFinder.class);
        factory.setProperty ("minGap", "00:00:20");
        factory.setProperty ("timeZone", "America/New_York");
        factory.setProperty ("openTime", "9:30");
        factory.setProperty ("closeTime", "15:30");
        
        runner.setProcessorFactory (factory);
        //*********************************************************************
        //  Open the GUI
        //*********************************************************************
//        SwingUtil.setWindowsLookAndFeel ();
        try {
            UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable ignored) {
        }

        DemoFrame               frame = new DemoFrame (runner);
        
        frame.setTitle ("Processing " + streamKey);
        frame.setVisible (true);            
    }        
}
