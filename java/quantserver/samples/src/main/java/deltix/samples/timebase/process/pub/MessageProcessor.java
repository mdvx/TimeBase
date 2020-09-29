package deltix.samples.timebase.process.pub;

import deltix.timebase.api.messages.InstrumentMessage;

/**
 *  Implemented by user-defined message processors, which can be executed
 *  by this demo framework.
 */
public interface MessageProcessor {
    /**
     *  Called by the framework before messages begin to be fed.
     */
    public void             init (ProcessingJob job);
    
    /**
     *  This method is called consecutively on incoming messages.
     *  
     *  @param msg          The message to process.
     * 
     *  @return             Whether to continue processing this job.
     */
    public boolean          process (InstrumentMessage msg); 
    
    /**
     *  Called by the framework after processing is stopped.
     */
    public void             close ();
}
