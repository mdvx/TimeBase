package deltix.qsrv.hf.pub.codec;

import deltix.util.memory.MemoryDataOutput;
import deltix.qsrv.hf.pub.md.*;
/**
 *
 */
public interface PolyUnboundEncoder extends UnboundEncoder {
    public void                 beginWrite (
        RecordClassDescriptor       rcd, 
        MemoryDataOutput            out
    );    
}
