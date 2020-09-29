package deltix.samples.timebase.process.pub;

import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;

/**
 *
 */
public abstract class ProcessingJob {
    public final InstrumentMessageSource        cursor;
    public final long                           endTime;
    public final double                         progress;
    
    protected ProcessingJob (
        InstrumentMessageSource                 cursor, 
        long                                    endTime,
        double                                  progress
    )
    {
        this.cursor = cursor;
        this.endTime = endTime;
        this.progress = progress;
    }
    
    public abstract String                      getName ();
    
    public void                                 close () {
        cursor.close ();
    }
}
