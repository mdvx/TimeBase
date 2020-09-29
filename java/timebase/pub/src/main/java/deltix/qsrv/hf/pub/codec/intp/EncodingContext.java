package deltix.qsrv.hf.pub.codec.intp;

import deltix.qsrv.hf.pub.codec.RecordLayout;
import deltix.util.memory.MemoryDataOutput;

/**
 *
 */
public class EncodingContext extends CodecContext {
    public MemoryDataOutput        out;

    EncodingContext (RecordLayout layout) {
        super (layout);
    }        
}
