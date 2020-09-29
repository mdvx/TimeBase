package deltix.samples.timebase.process.pub;

/**
 *
 */
public class ConsoleMultiProgressListener extends AbstractMultiProgressListener {
    public static final ConsoleMultiProgressListener    INSTANCE =
        new ConsoleMultiProgressListener ();
    
    @Override
    public void processingStarted (int numThreads) {
        System.out.println ("Starting " + numThreads + " workers...");
    }

    @Override
    public void jobStarted (int threadIdx, ProcessingJob job) {
        System.out.println ("Worker " + threadIdx + " begins processing " + job.getName () + "...");
    }

    @Override
    public void jobFinished (int threadIdx, long numMessages) {
        System.out.println ("Worker " + threadIdx + " done; " + numMessages + " messages processed.");
    }

    @Override
    public void workerFinished (int threadIdx) {
        System.out.println ("Worker " + threadIdx + " is done.");
    }

    @Override
    public void workerTerminatedWithError (int threadIdx, Throwable x) {
        System.out.println ("Worker " + threadIdx + " was killed.");
        x.printStackTrace ();
    }

    @Override
    public void workerStarted (int threadIdx) {
        System.out.println ("Worker " + threadIdx + " is starting...");
    }    
}
