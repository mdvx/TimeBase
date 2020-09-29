package deltix.samples.timebase.process.part;

import deltix.qsrv.hf.blocks.InstrumentSet;
import deltix.timebase.api.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.query.*;
import deltix.samples.timebase.process.pub.*;

import java.util.ArrayList;

/**
 *
 */
public class PartitionBySymbol extends RangeScanJobAllocator {    
    protected final ArrayList <IdentityKey>      keys =
        new ArrayList <IdentityKey> ();
    
    private int                                         count = 0;
    
    public PartitionBySymbol () {
    }  
       
    /**
     *  Loads all entities from streams. Override to modify behavior.
     */
    protected void                  setUpEntities () {
        if (streams == null)
            throw new IllegalStateException ("Stream list has not been set");
        
        InstrumentSet           entset = new InstrumentSet ();
        
        for (DXTickStream s : streams)
            for (IdentityKey id : s.listEntities ())
                entset.add (id);
        
        keys.addAll (entset);
    }
    
    @Override
    public void                     createPlan () {
        createPlanCheck ();
        
        keys.clear ();
        
        setUpEntities ();
        
        count = 0;                
    }
    
    @Override
    public ProcessingJob            getNextJob () {
        int                                 totalKeys = keys.size ();
        IdentityKey key;
        long                                thisStartTime;
        long                                thisEndTime;
                    
        for (;;) {
            if (count == totalKeys)
                return (null);
            
            key = keys.get (count++);
            //
            //  UNION stream ranges
            //
            thisStartTime = Long.MAX_VALUE;
            thisEndTime = Long.MIN_VALUE;

            for (DXTickStream s : streams) {
                long []                     tr = s.getTimeRange (key);

                if (tr == null) {
                    System.out.println ("skipping " + key + " - no data in TB at all");
                    continue;
                }

                if (tr [0] < thisStartTime)
                    thisStartTime = tr [0];

                if (tr [1] > thisEndTime)
                    thisEndTime = tr [1];
            }            
            //
            //  Use preset range to NARROW the effective range
            //
            if (startTime > thisStartTime)
                thisStartTime = startTime;
                
            if (endTime < thisEndTime)
                thisEndTime = endTime;
            
            if (thisStartTime != Long.MAX_VALUE)    // Symbol has data
                break;       
            
            System.out.println ("skipping " + key + " - time range does not intersect the required one");
        }
                
        InstrumentMessageSource     cursor = 
            openCursor (
                thisStartTime, 
                new IdentityKey[] { key }
            );
        
        return (new EntityJob (key, cursor, thisEndTime, ((double) count) / totalKeys));
    }   
}
