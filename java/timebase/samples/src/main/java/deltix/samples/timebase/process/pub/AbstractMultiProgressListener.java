package deltix.samples.timebase.process.pub;

/**
 *  Empty implementation for all methods of {@link MultiProgressListener}
 */
public class AbstractMultiProgressListener implements MultiProgressListener {

    @Override
    public void reportProgress (int threadIdx, long numMessages,
                                double amountDone) {
    }

    @Override
    public void processingStarted (int numThreads) {
    }

    @Override
    public void processingFinished () {        
    }
    
    @Override
    public void jobStarted (int threadIdx, ProcessingJob job) {
    }

    @Override
    public void jobFinished (int threadIdx, long numMessages) {
    }

    @Override
    public void workerFinished (int threadIdx) {
    }

    @Override
    public void workerTerminatedWithError (int threadIdx, Throwable x) {
    }

    @Override
    public void workerStarted (int threadIdx) {
    }
    
}
