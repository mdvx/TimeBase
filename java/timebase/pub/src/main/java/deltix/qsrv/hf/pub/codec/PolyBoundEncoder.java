package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.util.memory.MemoryDataOutput;

/**
 *
 */
public interface PolyBoundEncoder {
    /**
     *  Encodes an object, starting at current position of the 
     *  specified MemoryDataOutput.
     * 
     *  @param out      A MemoryDataOutput to write to.
     *  @param message  A message object to encode.
     */
    public void                 encode (
        Object                      message,
        MemoryDataOutput            out
    );
    
    /**
     *  Encodes an object, starting at current position of the 
     *  specified MemoryDataOutput. The RecordClassDescriptor argument 
     *  is necessary when there are multiple record class descriptors
     *  bound to the same Java class.
     * 
     *  @param rcd      The descriptor to use.
     *  @param out      A MemoryDataOutput to write to.
     *  @param message  A message object to encode.
     */
    public void                 encode (
        RecordClassDescriptor       rcd,
        Object                      message,
        MemoryDataOutput            out
    );
}
