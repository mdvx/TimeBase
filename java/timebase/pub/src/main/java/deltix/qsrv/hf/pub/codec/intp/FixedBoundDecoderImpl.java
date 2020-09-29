package deltix.qsrv.hf.pub.codec.intp;

import deltix.qsrv.hf.pub.codec.*;
import deltix.util.memory.MemoryDataInput;

/**
 *
 */
public final class FixedBoundDecoderImpl
    extends FixedBoundExternalDecoderImpl 
    implements BoundDecoder
{
    private Object          message;
    
    public FixedBoundDecoderImpl (RecordLayout layout) {
        super (layout);  
        message = layout.newInstance ();
        setStaticFields (message);
    }
    
    public Object           decode (MemoryDataInput in) {
        super.decode (in, message);
        return (message);
    }

    public Object           decode (DecodingContext ctx) {
        super.decode (ctx, message);
        return (message);
    }
}
