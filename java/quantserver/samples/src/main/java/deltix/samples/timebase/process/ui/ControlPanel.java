package deltix.samples.timebase.process.ui;

import java.awt.event.*;
import javax.swing.*;

/**
 *  Controls the process runner.
 */
public class ControlPanel extends JToolBar {
    private final DemoFrame             demo;
    
    private final JComboBox<Integer> numWorkersBox = new JComboBox<Integer>();
    private final Action                startAction =
        new AbstractAction ("Start") {
            @Override
            public void                 actionPerformed (ActionEvent e) {
                demo.start ();
            }
        };
    
    private final Action                stopAction =
        new AbstractAction ("Stop") {
            @Override
            public void                 actionPerformed (ActionEvent e) {
                demo.stop ();
            }
        };
    
    public ControlPanel (DemoFrame demo) {
        this.demo = demo;
        
        stopAction.setEnabled (false);
        
        for (int ii = 1; ii <= 32; ii++)
            numWorkersBox.addItem (ii);
                   
        numWorkersBox.setSelectedItem (demo.runner.getNumWorkers ());
        
        numWorkersBox.addActionListener (
            new ActionListener () {
                @Override
                public void     actionPerformed (ActionEvent e) {
                    ControlPanel.this.demo.numWorkersChanged (
                        (Integer) numWorkersBox.getSelectedItem ()
                    );
                }
            }
        );
        
        add (new JLabel ("# Workers: "));
        add (numWorkersBox);
        
        addSeparator ();
        
        add (startAction);
        add (stopAction);
    }     
    
    void                setRunning (boolean isRunning) {
        numWorkersBox.setEnabled (!isRunning);                
        startAction.setEnabled (!isRunning);                
        stopAction.setEnabled (isRunning);
    }
}
