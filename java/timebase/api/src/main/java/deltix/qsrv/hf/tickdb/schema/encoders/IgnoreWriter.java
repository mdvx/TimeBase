package deltix.qsrv.hf.tickdb.schema.encoders;

import deltix.qsrv.hf.pub.WritableValue;
import deltix.qsrv.hf.pub.md.DataType;

public class IgnoreWriter extends DefaultValueEncoder {

    public IgnoreWriter(WritableValue encoder, DataType type) {
        super(encoder, type);
    }

    @Override
    public void writeDefault() {
        // do nothing
    }

    @Override
    public MixedWritableValue clone(WritableValue out) {
        return new IgnoreWriter(out, type);
    }
}
