package deltix.samples.timebase.process.ui;

import deltix.samples.timebase.process.pub.ProcessingJob;
import deltix.util.concurrent.UncheckedInterruptedException;
import java.awt.*;
import javax.swing.*;

/**
 *
 */
public class WorkerPanel extends JPanel {
    private static final int    SCALE = 10000;
    
    private JLabel []           statusLabels;
    private JProgressBar []     progressBars;
    
    public WorkerPanel () {
        super (new GridBagLayout ());        
    }
    
    private static JLabel       mkHeader (String text) {
        JLabel  h = new JLabel (text);
        h.setOpaque (true);
        h.setBackground (Color.lightGray);
        h.setHorizontalAlignment (JLabel.CENTER);
        return (h);
    }
    
    public void                 setWorkerStarted (int idx) {
        statusLabels [idx].setText ("");
    }
    
    public void                 setWorkerFinished (int idx) {
        setWorkerFinished (idx, "Done", Color.green);                      
    }
    
    public void                 setWorkerCrashed (int idx, Throwable x) {
        if (x instanceof InterruptedException || x instanceof UncheckedInterruptedException)
            setWorkerFinished (idx, "Interrupted", Color.yellow);
        else {
            x.printStackTrace ();
            setWorkerFinished (idx, "Error", Color.red);
        }
    }
    
    private void                setWorkerFinished (int idx, String text, Color color) {
        JProgressBar        pbar = progressBars [idx];
        
        pbar.setValue (0);
        pbar.setString ("");
        
        JLabel      lbl = statusLabels [idx];
        
        lbl.setBackground (color);
        lbl.setText (text);                    
    }
    
    public void                 setProgress (
        int                         idx, 
        long                        numMessages, 
        double                      amtDone
    )
    {
        JProgressBar        pbar = progressBars [idx];
        
        pbar.setValue ((int) (amtDone * SCALE));
        pbar.setString (numMessages + " msgs");
    }
    
    public void                 setJobStarted (int idx, ProcessingJob job) {
        setProgress (idx, 0, 0);
        
        JLabel              lbl = statusLabels [idx];
        
        lbl.setText (job.getName ());
        lbl.setBackground (Color.cyan);
    }
    
    public void                 setUp (int numWorkers) {
        statusLabels = new JLabel [numWorkers];
        progressBars = new JProgressBar [numWorkers];
        
        removeAll ();
        
        GridBagConstraints      gc = new GridBagConstraints ();
        
        gc.weighty = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;        
        gc.gridy = 0;
        gc.gridx = 0;        
        add (mkHeader ("Worker #"), gc);
        gc.gridx++;        
        add (mkHeader ("Job"), gc);
        gc.gridx++;        
        add (mkHeader ("Progress"), gc);
        
        for (int ii = 0; ii < numWorkers; ii++) {
            JLabel              workerNumberLabel = new JLabel (String.valueOf (ii + 1));
            
            workerNumberLabel.setHorizontalAlignment (JLabel.CENTER);
            
            gc.gridy = ii + 1;
            gc.gridx = 0;
            gc.weightx = 0;            
            add (workerNumberLabel, gc);
            
            JLabel              statusLabel = new JLabel ();
            
            statusLabel.setOpaque (true);
            statusLabel.setHorizontalAlignment (JLabel.CENTER);
            
            gc.gridx = 1;
            gc.weightx = 1;
            add (statusLabel, gc);
            
            statusLabels [ii] = statusLabel;
            
            JProgressBar        pbar = new JProgressBar (0, SCALE);
            
            pbar.setStringPainted (true);
            
            gc.gridx = 2;
            gc.weightx = 1;
            add (pbar, gc);
            
            progressBars [ii] = pbar;            
        }
        
        //  Add a spring
        gc.gridy++;
        gc.gridx = 0;
        gc.weighty = 1;
        add (new JLabel (), gc);
        
        revalidate ();
        repaint ();
    }
}
