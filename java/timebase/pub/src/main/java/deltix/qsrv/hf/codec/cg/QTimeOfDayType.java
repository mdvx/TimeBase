package deltix.qsrv.hf.codec.cg;

import deltix.qsrv.hf.pub.md.TimeOfDayDataType;
import deltix.util.jcg.JCompoundStatement;
import deltix.util.jcg.JExpr;

import static deltix.qsrv.hf.tickdb.lang.compiler.cg.QCGHelpers.CTXT;

public class QTimeOfDayType extends QPrimitiveType <TimeOfDayDataType> {

    public QTimeOfDayType(TimeOfDayDataType dt) {
        super(dt);
    }

    @Override
    protected JExpr decodeExpr(JExpr input) {
        return input.call("readInt");
    }

    @Override
    protected void encodeExpr(JExpr output, JExpr value, JCompoundStatement addTo) {
        addTo.add(output.call("writeInt", value));
    }

    @Override
    public Class<?> getJavaClass() {
        return int.class;
    }

    @Override
    public JExpr getNullLiteral() {
        return CTXT.staticVarRef(TimeOfDayDataType.class, "NULL");
    }

    @Override
    public JExpr makeConstantExpr(Object obj) {
        return CTXT.intLiteral(((Number) obj).intValue());
    }

    @Override
    protected void encodeNullImpl(JExpr output, JCompoundStatement addTo) {
        addTo.add(output.call("writeInt", getNullLiteral()));
    }

    @Override
    public int getEncodedFixedSize() {
        return 4;
    }
}
