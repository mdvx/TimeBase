package deltix.samples.timebase.process.part;

/**
 *
 */
public abstract class RangeScanJobAllocator extends MessageScanJobAllocator {
    protected long                  startTime = Long.MIN_VALUE;
    protected long                  endTime = Long.MAX_VALUE;
    
    public RangeScanJobAllocator () {        
    }

    /**
     *  Configures the end time for data retrieval (inclusive).
     * 
     *  @param time     Inclusive end time.
     */
    public void                     setEndTime (long time) {
        this.endTime = time;
    }

    /**
     *  Configures the start time for data retrieval (inclusive).
     * 
     *  @param time     Inclusive start time.
     */
    public void                     setStartTime (long time) {
        this.startTime = time;
    }
        
    @Override
    protected void                  createPlanCheck () {
        super.createPlanCheck ();
        
        if (endTime <= startTime)
            throw new IllegalStateException (
                "Illegal time range: " + startTime + ".." + endTime
            );
    }    
}
