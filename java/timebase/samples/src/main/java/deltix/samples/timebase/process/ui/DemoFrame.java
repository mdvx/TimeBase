package deltix.samples.timebase.process.ui;

import deltix.qsrv.hf.tickdb.comm.client.TickDBClient;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.samples.timebase.process.demos.*;
import deltix.samples.timebase.process.part.*;
import deltix.samples.timebase.process.pub.*;

import deltix.util.swing.*;
import deltix.util.swing.Console;

import java.awt.*;
import java.io.*;
import javax.swing.*;

/**
 *
 */
public class DemoFrame extends JFrame {
    final ProcessRunner         runner;    
    final ControlPanel          controlPanel;
    final WorkerPanel           workerPanel;
    final PerformancePanel      performancePanel;    
    final Console               console = new Console (5, 80);
    long []                     messageCounts;
    ProcessingJob []            jobs;
    
    private final MultiProgressListener progressListener =
        new AbstractMultiProgressListener () {
            @Override
            public void processingStarted (int numThreads) {
                controlPanel.setRunning (true);
                performancePanel.setStarted ();
                
                messageCounts = new long [numThreads];
                jobs = new ProcessingJob [numThreads];
            }

            @Override
            public void processingFinished () {
                controlPanel.setRunning (false);
                performancePanel.setFinished ();
            }

            @Override
            public void workerStarted (int threadIdx) {
                workerPanel.setWorkerStarted (threadIdx);
            }
            
            @Override
            public void workerFinished (int threadIdx) {
                workerPanel.setWorkerFinished (threadIdx);
            }

            @Override
            public void workerTerminatedWithError (
                int                         threadIdx, 
                Throwable                   x
            ) 
            {
                workerPanel.setWorkerCrashed (threadIdx, x);
            }

            @Override
            public void jobStarted (int threadIdx, ProcessingJob job) {
                jobs [threadIdx] = job;
                workerPanel.setJobStarted (threadIdx, job);
                messageCounts [threadIdx] = 0;
            }

            @Override
            public void reportProgress (
                int         threadIdx, 
                long        numMessages,
                double      amountDone
            )
            {
                workerPanel.setProgress (threadIdx, numMessages, amountDone);
                
                long        prevNumMessages = messageCounts [threadIdx];
                
                performancePanel.incrementMessageCount (numMessages - prevNumMessages);
                
                messageCounts [threadIdx] = numMessages;
            }

            @Override
            public void jobFinished (int threadIdx, long numMessages) {
                reportProgress (threadIdx, numMessages, 1.0);       
                performancePanel.setJobProgress (jobs [threadIdx].progress);                
            }           
        };
    
    public DemoFrame (ProcessRunner runner) {
        this.runner = runner;
        
        runner.setProgressListener (
            new ProgressListenerSynchronizer (
                progressListener
            )
        );
        
        controlPanel = new ControlPanel (this);
        workerPanel = new WorkerPanel ();
        performancePanel = new PerformancePanel ();        
        
        workerPanel.setUp (runner.getNumWorkers ());
        
        Container               cp = getContentPane ();
        
        cp.add (controlPanel, BorderLayout.NORTH);
        
        JSplitPane              s1 = 
            new JSplitPane (
                JSplitPane.HORIZONTAL_SPLIT, 
                new JScrollPane (workerPanel), 
                performancePanel
            );
        
        s1.setDividerLocation (400);
        
        JSplitPane              s2 = 
            new JSplitPane (JSplitPane.VERTICAL_SPLIT, s1, console);
        
        s2.setDividerLocation (500);
        
        cp.add (s2, BorderLayout.CENTER);
        
        setDefaultCloseOperation (EXIT_ON_CLOSE);
        setSize (1024, 800);
        
        try {
            OutputStream    consoleStream = console.openStream ("UTF-8");
            PrintStream     ps = new PrintStream (consoleStream, true, "UTF-8");

            System.setOut (ps);
            System.setErr (ps);
        } catch (UnsupportedEncodingException x) {
            throw new RuntimeException (x);
        }
    }
    
    void                        numWorkersChanged (int n) {
        runner.setNumWorkers (n);
        workerPanel.setUp (n);
    }
    
    void                        start () {
        console.clear ();
        runner.start ();
    }
    
    void                        stop () {
        runner.stop ();
    }
}
