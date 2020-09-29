package deltix.samples.timebase.process.ui;

import deltix.samples.timebase.process.pub.*;
import javax.swing.SwingUtilities;

/**
 *  Synchronizes all PL events to be fired inside the AWT Event thread.
 */
public class ProgressListenerSynchronizer implements MultiProgressListener {
    private final MultiProgressListener     syncDelegate;

    public ProgressListenerSynchronizer (MultiProgressListener syncDelegate) {
        this.syncDelegate = syncDelegate;
    }        
    
    @Override
    public void     processingStarted (final int numThreads) {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.processingStarted (numThreads);
                }
            }
        );
    }

    @Override
    public void     processingFinished () {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.processingFinished ();
                }
            }
        );
    }
    
    @Override
    public void     reportProgress (
        final int             threadIdx, 
        final long            numMessages,
        final double          amountDone
    )
    {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.reportProgress (threadIdx, numMessages, amountDone);
                }
            }
        );
    }

    @Override
    public void     jobStarted (final int threadIdx, final ProcessingJob job) {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.jobStarted (threadIdx, job);
                }
            }
        );
    }

    @Override
    public void     jobFinished (final int threadIdx, final long numMessages) {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.jobFinished (threadIdx, numMessages);
                }
            }
        );
    }

    @Override
    public void     workerFinished (final int threadIdx) {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.workerFinished (threadIdx);
                }
            }
        );
    }

    @Override
    public void     workerTerminatedWithError (
        final int                   threadIdx, 
        final Throwable             x
    )
    {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.workerTerminatedWithError (threadIdx, x);
                }
            }
        );
    }

    @Override
    public void     workerStarted (final int threadIdx) {
        SwingUtilities.invokeLater (
            new Runnable () {
                @Override
                public void run () {
                    syncDelegate.workerStarted (threadIdx);
                }
            }
        );
    }
}
