package deltix.qsrv.hf.pub.codec.intp;

import deltix.qsrv.hf.codec.cg.ObjectManager;
import deltix.qsrv.hf.pub.codec.RecordLayout;
import deltix.util.memory.MemoryDataInput;

/**
 *
 */
public class DecodingContext extends CodecContext {
    public ObjectManager            manager;
    private boolean                 standalone = false;

    public MemoryDataInput                  in;
    
    public DecodingContext (RecordLayout layout) {
        super (layout);
        manager = new ObjectManager();
        standalone = true;
    }

    DecodingContext (RecordLayout layout, ObjectManager manager) {
        super (layout);
        this.manager = manager;
    }

    CharSequence            readCharSequence(MemoryDataInput in) {
        return manager.readCharSequence(in);
    }

    public void             setInput(MemoryDataInput input) {
        this.in = input;
        if (standalone)
            this.manager.clean();
    }

    public void             setInput(DecodingContext ctx) {
        this.in = ctx.in;
        this.manager = ctx.manager;
        this.standalone = false;
    }
}
