package deltix.samples.timebase.process.top;


import deltix.qsrv.hf.tickdb.comm.client.TickDBClient;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.samples.timebase.process.demos.PriceChangeCalculator;
import deltix.samples.timebase.process.part.SpecificSignalJobAllocator;
import deltix.samples.timebase.process.pub.ProcessRunner;
import deltix.samples.timebase.process.ui.DemoFrame;
import deltix.util.csvx.CSVXReader;
import deltix.util.time.Interval;

import javax.swing.*;
import java.util.TimeZone;

/**
 *  Runs the IntradayDataGapFinder processor in the GUI framework.
 */
public class PriceChangeCalculatorGUITest {
    public static void      main (String [] args) throws Exception {
        String                  host = "localhost";
        int                     port = 8011;
        String                  streamKey = "bars1min";
        String                  signalsFile = "c:/Users/Gene/Downloads/usTrades2.csv";
        
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
        //  MAIN INIT
        //*********************************************************************
        TimeZone                tz = TimeZone.getTimeZone ("America/New_York");
        
        PriceChangeCalculator.Factory   factory = 
            new PriceChangeCalculator.Factory ();
        
        factory.setTimeZone (tz);
        //
        //  "i" means MINUTE in our Interval system. 
        //  The rest of units is self-explanatry: y,m (month),d,h and s
        //
        factory.setIntervals (
            Interval.valueOf ("5i"),
            Interval.valueOf ("10i"),
            Interval.valueOf ("15i")
        );
        
        SpecificSignalJobAllocator  ja = new SpecificSignalJobAllocator ();

        ja.setStreams (stream);                   
        //
        //  Read the CSV file
        //
        CSVXReader                  csv = 
            new CSVXReader (signalsFile);
        
        while (csv.nextLine ()) {
            String                  date = csv.getString (0, true);
            String                  symbol = csv.getString (1, true);
            String                  param = csv.getString (2, true);
            
            ja.addSignal (
                factory.createSignal (
                    InstrumentType.EQUITY, symbol,
                    "yyyy-MM-dd HH:mm", date + " 9:30",
                    date + "," + symbol + "," + param
                )                
            );
        }
        
        csv.close ();
        
        runner.setJobAllocator (ja);
        
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
