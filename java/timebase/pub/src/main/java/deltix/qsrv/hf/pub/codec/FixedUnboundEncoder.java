package deltix.qsrv.hf.pub.codec;

import deltix.util.memory.MemoryDataOutput;

/**
 *
 */
public interface FixedUnboundEncoder extends UnboundEncoder, FixedCodec {
    public void                 beginWrite (MemoryDataOutput out);    
}
