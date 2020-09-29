package deltix.samples.timebase.process.pub;

import deltix.timebase.api.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;

/**
 *  Thread running processing jobs.
 */
class Worker extends Thread {
    /**
     *  Only call System.currentTimeMillis () once per this number of messages.
     */
    private static final long                   FUDGE_FACTOR = 1000;
    
    /**
     *  Only call progress listener once in this number of milliseconds.
     */
    private static final long                   REPORT_FREQUENCY = 50;
    
    private final ProcessRunner                 runner;
    private final int                           index;
    
    public Worker (
        ProcessRunner                   runner,
        int                             index
    )
    {
        super ("Worker #" + index);
        
        this.runner = runner;
        this.index = index;        
    }
        
    @Override
    public void             run () {
        MultiProgressListener   listener = runner.getProgressListener ();
        
        try {
            runInternal ();
            
            if (listener != null) 
                listener.workerFinished (index);
        } catch (Throwable x) {
            if (listener != null) 
                listener.workerTerminatedWithError (index, x);
        } finally {
            runner.notifyFinished (this, index);                        
        }
    }
    
    private void            runInternal () throws Exception {
        final MultiProgressListener listener = runner.getProgressListener ();
        final JobAllocator          jobAllocator = runner.getJobAllocator ();
        final MessageProcessorFactory processorFactory = runner.getProcessorFactory ();
        
        if (listener != null)
            listener.workerStarted (index);

        for (;;) {
            ProcessingJob       job;

            synchronized (jobAllocator) {
                job = jobAllocator.getNextJob ();
            }

            if (job == null) 
                break;            
            
            MessageProcessor    mproc = null;
            
            try {
                if (listener != null)
                    listener.jobStarted (index, job);

                mproc = processorFactory.newProcessor ();
                
                mproc.init (job);
                
                final InstrumentMessageSource cursor = job.cursor;
                final long                  endTime = job.endTime;
                long                        startMsgTime = Long.MIN_VALUE;
                double                      timeSpan = 0;
                long                        lastReportTime = System.currentTimeMillis ();
                long                        numMessagesProcessed = 0;

                while (cursor.next ()) {
                    final InstrumentMessage msg = cursor.getMessage ();
                    final long              thisMsgTime = msg.getTimeStampMs();

                    if (thisMsgTime > job.endTime)
                        break;
                    
                    boolean                 cont = mproc.process (msg);

                    if (!cont)
                        break;
                    
                    numMessagesProcessed++;

                    if (listener != null) {
                        if (startMsgTime == Long.MIN_VALUE) {
                            startMsgTime = thisMsgTime;
                            timeSpan = endTime - thisMsgTime;
                        }
                        else if (numMessagesProcessed % FUDGE_FACTOR == 0) {
                            long            now = System.currentTimeMillis ();
                            
                            if (now >= lastReportTime + REPORT_FREQUENCY) {
                                listener.reportProgress (
                                    index,
                                    numMessagesProcessed, 
                                    (thisMsgTime - startMsgTime) / timeSpan
                                );
                                
                                lastReportTime = now;
                            }
                        }
                    }
                }

                if (listener != null)
                    listener.jobFinished (index, numMessagesProcessed);           
            } finally {
                job.close ();   
                
                if (mproc != null)
                    mproc.close ();
            }
        }                
    }                
}    
