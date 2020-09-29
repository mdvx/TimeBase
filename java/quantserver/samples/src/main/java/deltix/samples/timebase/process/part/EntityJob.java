package deltix.samples.timebase.process.part;

import deltix.timebase.api.messages.ConstantIdentityKey;
import deltix.timebase.api.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;
import deltix.samples.timebase.process.pub.ProcessingJob;

/**
 *
 */
public class EntityJob extends ProcessingJob {
    private final ConstantIdentityKey entity;

    EntityJob (
        IdentityKey          entity,
        InstrumentMessageSource     cursor,
        long                        endTime, 
        double                      progress
    )
    {
        super (cursor, endTime, progress);
        
        this.entity = ConstantIdentityKey.makeImmutable (entity);
    }

    @Override
    public String                   getName () {
        return entity.toString ();
    }    
}
