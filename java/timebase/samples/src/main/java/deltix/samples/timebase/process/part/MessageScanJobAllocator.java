package deltix.samples.timebase.process.part;

import deltix.samples.timebase.process.pub.*;
import deltix.timebase.api.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;

/**
 *
 */
public abstract class MessageScanJobAllocator implements JobAllocator {
    protected DXTickStream []       streams;
    protected String []             types = null;
    public final SelectionOptions   options = new SelectionOptions ();
    
    public MessageScanJobAllocator () {        
    }

    /**
     *  Configures a list of streams to read data from.
     * 
     *  @param streams  A list of streams.
     */
    public void                     setStreams (DXTickStream ... streams) {
        if (streams == null || streams.length == 0)
            throw new IllegalArgumentException ("Empty stream list");
        
        this.streams = streams;
    }

    /**
     *  Configures a list of message types to accept.
     * 
     *  @param types  A list of types, or <code>null</code> to select all.
     */
    public void                     setTypes (String ... types) {
        this.types = types;
    }

    protected void                  createPlanCheck () {
        if (streams == null)
            throw new IllegalStateException ("Stream list has not been set");        
    }
    
    protected InstrumentMessageSource   openCursor (
        long                                time,
        IdentityKey []               entities
    )
    {
        return (
            streams [0].getDB ().select (
                time, 
                options, 
                types,
                entities, 
                streams
            )
        );
    }
}
