package deltix.qsrv.dtb.store.pub;

import deltix.util.memory.MemoryDataOutput;

/**
 *  Produces a message.
 */
public interface TSMessageProducer {    
    public void  writeBody (MemoryDataOutput out);
}