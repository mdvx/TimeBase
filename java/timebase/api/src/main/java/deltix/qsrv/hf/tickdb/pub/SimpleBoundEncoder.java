package deltix.qsrv.hf.tickdb.pub;

import deltix.data.stream.MessageEncoder;
import deltix.qsrv.hf.codec.CodecUtils;
import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.TypeLoader;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedBoundEncoder;
import deltix.qsrv.hf.pub.codec.RecordTypeMap;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.memory.MemoryDataOutput;

/**
 * Created by Alex Karpovich on 29/07/2020.
 */
public class SimpleBoundEncoder implements MessageEncoder<InstrumentMessage> {

    private final TypeLoader loader;

    private final RecordTypeMap<Class> typeMap;
    private final FixedBoundEncoder[] encoders;
    private final boolean[] validated; // indicated if encoder is validated
    private int code;

    public SimpleBoundEncoder(CodecFactory factory, TypeLoader loader, RecordClassDescriptor[] types) {
        this.loader = loader;

        final int numTypes = types.length;

        encoders = new FixedBoundEncoder[numTypes];
        validated = new boolean[numTypes];

        final Class<?>[] classes = new Class<?>[numTypes];

        for (int ii = 0; ii < numTypes; ii++) {
            if (types[ii] == null)
                continue;

            FixedBoundEncoder encoder = factory.createFixedBoundEncoder(loader, types[ii]);

            CodecUtils.validateBoundClass(encoder.getClassInfo());

            encoders[ii] = encoder;
            classes[ii] = encoder.getClassInfo().getTargetClass();
        }

        typeMap = new RecordTypeMap<>(classes);
    }

    @Override
    public boolean encode(InstrumentMessage message, MemoryDataOutput out) {
        code = typeMap.getCode(message.getClass());

        if (validated.length > 1)
            out.writeUnsignedByte(code);

        out.writeString(message.getSymbol());

        FixedBoundEncoder encoder = encoders[code];

        if (!validated[code]) {
            CodecUtils.validateBoundClass(encoder.getClassInfo());
            validated[code] = true;
        }

        encoder.encode(message, out);
        return true;
    }

    @Override
    public int getContentOffset() {
        return 0;
    }

    @Override
    public int getTypeIndex() {
        return 0;
    }
}
