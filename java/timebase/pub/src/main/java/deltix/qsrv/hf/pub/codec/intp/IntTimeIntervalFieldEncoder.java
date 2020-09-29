package deltix.qsrv.hf.pub.codec.intp;

import deltix.qsrv.hf.pub.codec.*;
import deltix.qsrv.hf.pub.md.IntegerDataType;
import deltix.util.text.CharSequenceParser;

/**
 *
 */
class IntTimeIntervalFieldEncoder extends IntegerFieldEncoder {
    public IntTimeIntervalFieldEncoder (NonStaticFieldLayout f) {
        super (f);
    }

    @Override
    void                    writeNull(EncodingContext ctxt) {
        setLong(IntegerDataType.PINTERVAL_NULL, ctxt);
    }

    @Override
    protected boolean isNull(long value) {
        return value == IntegerDataType.PINTERVAL_NULL; 
    }

    @Override
    void                    setString (CharSequence value, EncodingContext ctxt) {
        setLong (CharSequenceParser.parseInt (value), ctxt);
    }

    @Override
    void        setLongImpl (long value, EncodingContext ctxt) {
        deltix.qsrv.hf.pub.codec.TimeIntervalCodec.write(value, ctxt.out);
    }    
}
