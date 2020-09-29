package deltix.qsrv.hf.pub.codec;

import deltix.util.memory.MemoryDataOutput;

/**
 *
 */
public interface FixedBoundEncoder extends FixedCodec {
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
}