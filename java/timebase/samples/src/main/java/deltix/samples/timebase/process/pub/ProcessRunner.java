package deltix.samples.timebase.process.pub;

/**
 *  Runs a number of jobs that read data from TimeBase.
 */
public class ProcessRunner {
    //
    //  CONFIGURATION
    //
    private MessageProcessorFactory             processorFactory;
    private JobAllocator                        jobAllocator;
    private MultiProgressListener               progressListener;
    private int                                 numWorkers = 
        Runtime.getRuntime ().availableProcessors ();
    //
    //  STATE, guarded by this
    //
    private Worker []                           workers = null;
    
    /**
     *  Configures the partitioning strategy to use for parallelizing the work.
     */
    public synchronized void        setJobAllocator (
        JobAllocator                    jobAllocator
    )
    {
        syncAssertNotRunning ();
        
        this.jobAllocator = jobAllocator;
    }

    /**
     *  Configures the processor factory for creating individual processors.
     */
    public synchronized void        setProcessorFactory (
        MessageProcessorFactory         processorFactory
    )
    {
        syncAssertNotRunning ();
        
        this.processorFactory = processorFactory;
    }

    /**
     *  Configures the progress listener (optionally).
     */
    public synchronized void        setProgressListener (
        MultiProgressListener           progressListener
    )
    {
        syncAssertNotRunning ();
        
        this.progressListener = progressListener;
    }

    public synchronized MultiProgressListener       getProgressListener () {
        return progressListener;
    }

    public synchronized JobAllocator                getJobAllocator () {
        return jobAllocator;
    }

    public synchronized MessageProcessorFactory     getProcessorFactory () {
        return processorFactory;
    }
        
    /**
     *  Optionally configures the number of threads to use for parallel 
     *  processing. By default, this will be set to the # of CPUs available.
     */
    public synchronized void        setNumWorkers (int numWorkers) {
        syncAssertNotRunning ();
        
        if (numWorkers < 1)
            throw new IllegalArgumentException ("Illegal numWorkers: " + numWorkers);
        
        this.numWorkers = numWorkers;
    }

    /**
     *  Returns the currently configured number of workers 
     *  (whether explicitly or automatically)
     */
    public synchronized int         getNumWorkers () {
        return numWorkers;
    }    
    
    /**
     *  Begin processing. This method creates processing threads,
     *  partitions the work, and begins individual jobs. This method may not
     *  be called while the processor is running.
     * 
     *  @exception IllegalStateException    If the processor is already running.
     */
    public synchronized void        start () {        
        if (workers != null)
            throw new IllegalStateException (this + " is already running");
        
        if (progressListener != null)
            progressListener.processingStarted (numWorkers);
        
        jobAllocator.createPlan ();
        
        //
        //  Start threads.
        //
        workers = new Worker [numWorkers];
        
        for (int ii = 0; ii < numWorkers; ii++) {
            Worker  w = new Worker (this, ii);            
            
            w.start ();
            
            workers [ii] = w;
        }
    }
    
    void                            notifyFinished (Worker w, int threadIdx) {
        boolean                 someStillRunning = false;
        MultiProgressListener   listener;
        
        synchronized (this) {
            //
            //  Ignore notification from abandoned workers.
            //
            if (workers == null || 
                workers.length <= threadIdx || 
                w != workers [threadIdx])
                return;

            workers [threadIdx] = null;
            
            for (Worker ww : workers) {
                if (ww != null) {
                    someStillRunning = true;
                    break;
                }
            }
            
            listener = progressListener;
            
            if (!someStillRunning)
                workers = null;
        }
        
        if (!someStillRunning && listener != null)
            listener.processingFinished ();
    }
    
    private void                    syncAssertNotRunning () {
        if (workers != null)
            throw new IllegalStateException ("Processor is running, cannot make changes.");
    }
    
    /**
     *  Returns true if data processing is underway.
     */
    public synchronized boolean     isRunning () {
        return (workers != null);
    }
    
    /**
     *  This method notifies all workers to terminate, but does not wait for 
     *  them to complete the self-destruction sequence. Call 
     *  {@link #waitUntilDone} to wait for complete termination.
     *  This method simply has no effect if the processor is not running, so it
     *  can be safely called at all times.
     */
    public synchronized void        stop () {
        if (workers == null)
            return;
        
        for (Worker w : workers) {
            if (w != null)
                w.interrupt ();
        }        
    }
    
    /**
     *  Waits until all workers have terminated, either normally, or due to a
     *  call to {@link #stop}. {@link #isRunning} returns <code>false</code> after
     *  this method returns <code>true</code>.
     *  This method returns <code>true</code> right away if the processor
     *  is not running, so it can be safely called at all times.
     * 
     *  @param timeoutMillis    Timeout in milliseconds.
     */
    public synchronized boolean     waitUntilDone (int timeoutMillis) 
        throws InterruptedException 
    {
        if (workers == null)
            return (true);
        
        long            timeLimit = 
            timeoutMillis == 0 ? 0 : System.currentTimeMillis () + timeoutMillis;
        
        for (Worker w : workers) {
            if (w != null) {
                w.join (timeoutMillis == 0 ? 0 : timeLimit - System.currentTimeMillis ());
                
                if (w.isAlive ())
                    return (false);
            }
        }
        
        // We have joined all threads.
        workers = null;
        return (true);
    }    
}
