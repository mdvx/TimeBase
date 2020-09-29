package deltix.samples.timebase.process.pub;

/**
 *  Shows the progress of work performed in multiple threads. Calls related
 *  to the same thread index are externally synchronized, but generally the
 *  methods of this class can be called concurrently.
 */
public interface MultiProgressListener {
    /**
     *  Resets the listener and notifies it of the number of threads that 
     *  will be working.
     * 
     *  @param numThreads   The number of threads.
     */
    public void                 processingStarted (int numThreads);
    
    /**
     *  Notifies the listener that the work has been finished.
     */
    public void                 processingFinished ();
    
    /**
     *  Notifies the listener that worker has started.
     * 
     *  @param threadIdx    The thread number.
     */
    public void                 workerStarted (int threadIdx);
    
    /**
     *  Notifies the listener that one of the threads has begun a job.
     * 
     *  @param threadIdx    The thread number.
     *  @param job          The job object.
     */
    public void                 jobStarted (int threadIdx, ProcessingJob job);
    
    /**
     *  Notifies the listener of progress.
     * 
     *  @param threadIdx    The thread number.
     *  @param numMessages  The number of processed messages.
     *  @param amountDone   The total amount of work done, between 0 and 1.
     */
    public void                 reportProgress (
        int                         threadIdx, 
        long                        numMessages,
        double                      amountDone
    );
    
    /**
     *  Notifies the listener that one of the threads has completed a job.
     * 
     *  @param threadIdx    The thread number.
     *  @param numMessages  The number of processed messages.
     */
    public void                 jobFinished (
        int                         threadIdx, 
        long                        numMessages
    );
    
    /**
     *  Notifies the listener that worker has completed all work.
     * 
     *  @param threadIdx    The thread number.
     */
    public void                 workerFinished (int threadIdx);
    
    /**
     *  Notifies the listener that worker is terminating due to either it, or 
     *  the job allocator, or something in the framework throwing an exception.
     * 
     *  @param threadIdx    The thread number.
     *  @param x            The exception.
     */
    public void                 workerTerminatedWithError (
        int                         threadIdx, 
        Throwable                   x
    );        
}
