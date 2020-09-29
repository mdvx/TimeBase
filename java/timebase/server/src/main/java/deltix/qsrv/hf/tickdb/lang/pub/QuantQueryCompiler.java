package deltix.qsrv.hf.tickdb.lang.pub;

import deltix.qsrv.hf.pub.md.DataType;
import deltix.qsrv.hf.tickdb.lang.compiler.sx.CompiledExpression;
import deltix.qsrv.hf.tickdb.pub.query.PreparedQuery;

/**
 *
 */
public interface QuantQueryCompiler {
    CompiledExpression  compile (Expression e, DataType expectedType);
    
    PreparedQuery       compileStatement (Statement s);
}
