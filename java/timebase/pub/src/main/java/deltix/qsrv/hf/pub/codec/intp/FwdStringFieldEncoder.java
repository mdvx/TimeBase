package deltix.qsrv.hf.pub.codec.intp;

import deltix.qsrv.hf.pub.codec.NonStaticFieldLayout;
import deltix.qsrv.hf.pub.FwdStringCodec;

/**
 *
 */
class FwdStringFieldEncoder extends StringFieldEncoder {
    FwdStringFieldEncoder (NonStaticFieldLayout f) {
        super (f);
    }

    @Override
    void            setString (CharSequence value, EncodingContext ctxt) {
        FwdStringCodec.write(value, ctxt.out);
    }
}
