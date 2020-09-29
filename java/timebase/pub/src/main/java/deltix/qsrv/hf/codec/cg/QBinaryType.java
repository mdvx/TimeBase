package deltix.qsrv.hf.codec.cg;

import deltix.qsrv.hf.pub.md.BinaryDataType;
import deltix.qsrv.hf.codec.BinaryCodec;
import deltix.util.jcg.JCompoundStatement;
import deltix.util.jcg.JExpr;

import static deltix.qsrv.hf.tickdb.lang.compiler.cg.QCGHelpers.CTXT;

public class QBinaryType extends QPrimitiveType  <BinaryDataType> {

    public QBinaryType(BinaryDataType dt) {
        super(dt);
    }

    @Override
    public int getEncodedFixedSize() {
        return SIZE_VARIABLE;
    }

    @Override
    public Class<?> getJavaClass() {
        return byte[].class;
    }

    @Override
    protected JExpr getNullLiteral() {
        return CTXT.nullLiteral();
    }

    @Override
    protected void encodeNullImpl(JExpr output, JCompoundStatement addTo) {
        addTo.add(CTXT.staticCall(BinaryCodec.class, "writeNull", output));
    }
}
