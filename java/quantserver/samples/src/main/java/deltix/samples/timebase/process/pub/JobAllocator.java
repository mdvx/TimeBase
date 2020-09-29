package deltix.samples.timebase.process.pub;

/**
 *  Interface implemented by specific strategies for partitioning a large
 *  data processing job, such as, for example, by symbol. Calls to this
 *  object are externally synchronized.
 */
public interface JobAllocator {        
    /**
     *  This method is called in a loop to get the next job.
     * 
     *  @return     Next job to process, or null if there is no more 
     *              work.
     */
    public ProcessingJob            getNextJob ();
    
    /**
     *  Called just prior to processing.
     */
    public void                     createPlan ();    
}
