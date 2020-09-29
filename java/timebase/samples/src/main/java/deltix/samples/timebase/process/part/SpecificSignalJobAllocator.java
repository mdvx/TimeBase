package deltix.samples.timebase.process.part;

import deltix.timebase.api.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.query.*;
import deltix.samples.timebase.process.pub.*;

import java.util.ArrayList;

/**
 *
 */
public class SpecificSignalJobAllocator extends MessageScanJobAllocator {
    public interface Signal {
        public IdentityKey getKey ();
        
        public long                     getStartTime ();
        
        public long                     getEndTime ();
    }
    
    public static class SignalJob extends EntityJob {
        public final Signal         signal;
        
        public SignalJob (
            Signal                  signal,
            InstrumentMessageSource cursor,
            double                  progress
        )
        {
            super (signal.getKey (), cursor, signal.getEndTime (), progress);
            
            this.signal = signal;
        }        
    }
    
    protected final ArrayList <Signal>  signals = new ArrayList <Signal> ();    
    private int                         count = 0;
    
    public SpecificSignalJobAllocator () {
    }             
    
    public void                     clearSignals () {
        signals.clear ();
    }
    
    public void                     addSignal (Signal s) {
        signals.add (s);
    }
    
    @Override
    public void                     createPlan () {
        count = 0;                
    }
    
    @Override
    public ProcessingJob            getNextJob () {
        int                                 totalSignals = signals.size ();
        
        if (count == totalSignals)
            return (null);
        
        Signal                              signal = signals.get (count++);        
                
        InstrumentMessageSource     cursor = 
            openCursor (
                signal.getStartTime (), 
                new IdentityKey[] { signal.getKey () }
            );
        
        return (new SignalJob (signal, cursor, ((double) count) / totalSignals));
    }   
}
