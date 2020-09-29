package deltix.qsrv.hf.tickdb.impl;

import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.TypeLoader;
import deltix.qsrv.hf.pub.codec.BoundExternalDecoder;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;

public class SimpleMessageDecoder {

    private final BoundExternalDecoder      decoder;
    private final InstrumentMessage         message;
    private final RecordClassDescriptor     type;
    private final MemoryDataInput           input;

    public SimpleMessageDecoder (
            TypeLoader                  loader,
            CodecFactory                factory,
            RecordClassDescriptor       fixedType
    )
    {
        type = fixedType;
        message = (InstrumentMessage) fixedType.newInstanceNoX(loader);

        decoder = factory.createFixedExternalDecoder(loader, fixedType);
        decoder.setStaticFields (message);
        input = new MemoryDataInput();
    }

    public InstrumentMessage decode(MemoryDataInput in, int length) {

        input.setBytes(in.getBytes(), in.getCurrentOffset(), length);
        decoder.decode(input, message);
        return message;
    }

    public InstrumentMessage getMessage() {
        return message;
    }

    public RecordClassDescriptor getType() {
        return type;
    }
}
