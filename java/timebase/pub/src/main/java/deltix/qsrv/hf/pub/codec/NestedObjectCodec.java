package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.codec.MessageSizeCodec;
import deltix.util.memory.MemoryDataInput;

/**
 *
 */
public class NestedObjectCodec {
    public final static int     NULL_CODE = 0xFF;
    
    public static void          skip (MemoryDataInput in) {
        int     size = MessageSizeCodec.read (in);
        in.skipBytes (size);
    }
}
