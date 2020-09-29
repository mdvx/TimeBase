package deltix.samples.timebase.process.ui;

import deltix.util.swing.*;
import deltix.util.time.TimeFormatter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.*;


/**
 *
 */
public class PerformancePanel extends VerticalForm {
    private static final int        SCALE = 10000;
    private static final double     MAX_RATE_CHG_PER_MS = 400;
    private static double [][]      RANGES = {
        { 9E6,      1E6,        0.25E6 },
        { 5E6,      1E6,        0.1E6 },
        { 3E6,      0.25E6,     0.05E6 },
        { 1.5E6,    0.25E6,     0.02E6 },
        { 1E6,      0.1E6,      0.01E6 },
    };
            
    private final Gauge             gauge =
        new Gauge () {
            @Override
            protected String makeLabel (double value) {
                if (value >= 1E6)
                    return (String.format ("%3.2fM", value / 1E6));
                
                return (String.format ("%1.0fK", value / 1E3));
            }
        };
    
    private final JLabel            timeRunningLabel = new JLabel ();
    private final JLabel            messagesProcessedLabel = new JLabel ();
    private final JLabel            processingRateLabel = new JLabel ();
    private final JProgressBar      overallProgressBar = new JProgressBar (0, SCALE);
    private long                    startTime;
    private long                    numMessagesProcessed;
    private long                    lastGaugeMoveTime;
    private long                    lastGaugeReport;
    private double                  maxRate;
    
    public PerformancePanel () {
        initGauge (RANGES.length - 1);
        
        gauge.setMargin (20);
        gauge.setPreferredSize (new Dimension (550, 400));  
        gauge.setDisplayString ("Processing Rate");
        
        overallProgressBar.setStringPainted (true);
        
        addRow (gauge, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER, false);
        
        addField ("Overall progress:", overallProgressBar);
        addField ("Total time:", timeRunningLabel);
        addField ("Messages processed:", messagesProcessedLabel);
        addField ("Average rate (msgs/s):", processingRateLabel);                
    }
    
    private void                    initGauge (int rangeIdx) {
        double []   data = RANGES [rangeIdx];
                    
        gauge.setRange (0, data [0]);
        gauge.setLabelValueWidth (data [1]);
        gauge.setTickValueWidth (data [2]);                
    }
    
    public void                     setStarted () {
        startTime = System.currentTimeMillis ();      
        maxRate = 0;
        numMessagesProcessed = 0;
        lastGaugeReport = 0;
        lastGaugeMoveTime = startTime;
        overallProgressBar.setValue (0);
    }
    
    public void                     setJobProgress (double rate) {
        int     newValue = (int) (rate * SCALE);
        
        if (overallProgressBar.getValue () < newValue)
            overallProgressBar.setValue (newValue);
    }
    
    private void                    showStats () {
        long            now = System.currentTimeMillis ();
        long            timeRunning = now - startTime;
        long            cumulativeRate = 
            timeRunning == 0 ? 0 : numMessagesProcessed * 1000 / timeRunning;
        
        messagesProcessedLabel.setText (String.format ("%,d", numMessagesProcessed));
        
        timeRunningLabel.setText (TimeFormatter.formatTimeOfDay (timeRunning));
        
        processingRateLabel.setText (
            timeRunning == 0 ? "" : String.format ("%,d", cumulativeRate)
        );
        
        long            timeSinceGaugeMoved = now - lastGaugeMoveTime;
        
        if (timeSinceGaugeMoved > 50) {
            long        msgsProcessedSinceLastReport = numMessagesProcessed - lastGaugeReport;
            double      rate = msgsProcessedSinceLastReport * 1000.0 / timeSinceGaugeMoved;        
            double      old = gauge.getValue ();
            double      d = rate - old;
            double      maxd = MAX_RATE_CHG_PER_MS * timeSinceGaugeMoved;
            
            if (d > maxd)
                rate = old + maxd;
            else if (d < -maxd)
                rate = old - maxd;

            if (rate > maxRate) {
                for (int ii = 1; ii < RANGES.length; ii++) {
                    double      x = RANGES [ii][0];

                    if (maxRate < x && rate >= x) {
                        initGauge (ii - 1);
                        break;
                    }
                }            

                maxRate = rate;
            }

            gauge.setValue (rate);
        
            lastGaugeReport = numMessagesProcessed;
            lastGaugeMoveTime = now;
        }
    }
    
    public void                     incrementMessageCount (long inc) {
        if (inc < 0)
            throw new IllegalArgumentException ("negative increment: " + inc);
        
        numMessagesProcessed += inc;
        
        showStats ();
    }
    
    public void                     setFinished () {
        showStats ();
        gauge.setValue (0);
    }
}
