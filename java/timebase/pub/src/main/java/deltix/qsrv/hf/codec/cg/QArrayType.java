package deltix.qsrv.hf.codec.cg;

import deltix.qsrv.hf.codec.MessageSizeCodec;
import deltix.qsrv.hf.pub.md.ArrayDataType;
import deltix.util.jcg.JCompoundStatement;
import deltix.util.jcg.JExpr;

import static deltix.qsrv.hf.tickdb.lang.compiler.cg.QCGHelpers.CTXT;

/**
 *
 */
public class QArrayType extends QPrimitiveType<ArrayDataType> {

    public QArrayType(ArrayDataType dt) {
        super(dt);
    }

    @Override
    public int getEncodedFixedSize() {
        return SIZE_VARIABLE;
    }

    @Override
    public Class<?> getJavaClass() {
        throw new UnsupportedOperationException(
                "Not implemented for " + getClass().getSimpleName()
        );
    }

    @Override
    public void skip(JExpr input, JCompoundStatement addTo) {
        addTo.add (input.call ("skipBytes", CTXT.staticCall(MessageSizeCodec.class, "read", input)));
    }

    @Override
    protected JExpr getNullLiteral() {
        return CTXT.nullLiteral();
    }

    @Override
    protected void encodeNullImpl(JExpr output, JCompoundStatement addTo) {
        addTo.add(CTXT.staticCall(MessageSizeCodec.class, "write", CTXT.intLiteral(0), output));
    }
}