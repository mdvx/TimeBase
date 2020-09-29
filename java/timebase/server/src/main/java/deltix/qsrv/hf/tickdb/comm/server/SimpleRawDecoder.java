package deltix.qsrv.hf.tickdb.comm.server;

import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.impl.RecordDecoder;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.io.UncheckedIOException;
import deltix.util.lang.Filter;
import deltix.util.lang.Util;
import deltix.util.memory.MemoryDataInput;

public class SimpleRawDecoder implements RecordDecoder<InstrumentMessage>
{
    private final RecordClassDescriptor[]      types;
    private byte []                            bytes = new byte [128];
    final RawMessage                           msg = new RawMessage();

    private final StringBuilder                symBuilder = new StringBuilder();

    private int code = -1;

    public SimpleRawDecoder (RecordClassDescriptor []  types)
    {
        if (types.length > 256)
            throw new IllegalArgumentException (
                    "Too many classes: " + types.length + " (max 256)"
            );

        this.types = types;

        msg.setSymbol(symBuilder);
    }

    @Override
    public InstrumentMessage               decode (
        Filter<? super InstrumentMessage> filter,
        MemoryDataInput in
    )
    {
        code = types.length > 1 ? in.readUnsignedByte () : 0;

        try {
            msg.type = types [code];
        } catch (IndexOutOfBoundsException x) {
            throw new UncheckedIOException(
                    "Type code too big: " + code + "; accepted: " +
                            RecordClassDescriptor.printNames (types)
            );
        }

        in.readStringBuilder (symBuilder);

        int length = in.getAvail ();

        if (bytes.length < length)
            bytes = new byte [Util.doubleUntilAtLeast (bytes.length, length)];

        in.readFully (bytes, 0, length);

        msg.offset = 0;
        msg.data = bytes;
        msg.length = length;

        return (filter == null || filter.accept (msg) ? msg : null);
    }

    @Override
    public RecordClassDescriptor    getCurrentType() {
        return types[code];
    }

    @Override
    public int                      getCurrentTypeIndex() {
        return code;
    }
}
