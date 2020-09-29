package deltix.qsrv.hf.codec.cg;

import deltix.qsrv.hf.codec.BinaryUtils;
import deltix.qsrv.hf.codec.CodecUtils;
import deltix.qsrv.hf.pub.md.VarcharDataType;
import deltix.util.collections.generated.ByteArrayList;
import deltix.util.jcg.JCompoundStatement;
import deltix.util.jcg.JExpr;

import static deltix.qsrv.hf.tickdb.lang.compiler.cg.QCGHelpers.CTXT;

/**
 *
 */
public class QBStringType extends QBoundType<QStringType> {

    public QBStringType(QStringType qType, Class<?> javaType, QAccessor accessor, QVariableContainerLookup lookupContainer) {
        super(qType, javaType, accessor);
    }

    @Override
    public void decode(JExpr input, JCompoundStatement addTo) {
        if (javaBaseType == String.class)
            super.decode(input, addTo);
        else if (javaBaseType == CharSequence.class)
            addTo.add(accessor.write( CTXT.call("readCharSequence", input)));
        else if(javaBaseType == ByteArrayList.class) {
            addTo.add(
                    accessor.write(
                            CTXT.staticCall(
                                    BinaryUtils.class,
                                    "assign",
                                    accessor.read(),
                                    CTXT.call("readCharSequence", input)
                            )
                    )
            );
        } else
            throw new IllegalArgumentException("unexpected type " + javaBaseType);
    }

    @Override
    public void encode(JExpr output, JCompoundStatement addTo) {
        if (javaBaseType == String.class) {
            addTo.add(CTXT.staticCall(CodecUtils.class, "setString", accessor.read(), output,
                    qType.dt.getEncodingType() == VarcharDataType.INLINE_VARSIZE ? CTXT.trueLiteral() : CTXT.falseLiteral()));
        } else if(javaBaseType == ByteArrayList.class) {
            addTo.add(
                    output.call(
                            "writeString",
                            CTXT.staticCall(
                                    BinaryUtils.class,
                                    "toStringBuilder",
                                    accessor.read()
                            )
                    )
            );
        } else
            super.encode(output, addTo);
    }

    @Override
    protected JExpr makeConstantExpr(Object obj) {
            return super.makeConstantExpr(obj);
    }
}
