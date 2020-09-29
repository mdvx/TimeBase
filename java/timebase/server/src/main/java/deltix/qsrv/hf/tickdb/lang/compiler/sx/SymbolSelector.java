package deltix.qsrv.hf.tickdb.lang.compiler.sx;

import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.lang.compiler.sem.QQLCompiler;

/**
 *
 */
public class SymbolSelector extends CompiledExpression <DataType> {
    public SymbolSelector () {
        super (StandardTypes.CLEAN_VARCHAR);
        name = "$" + QQLCompiler.KEYWORD_SYMBOL;
    }

    @Override
    protected void                  print (StringBuilder out) {
        out.append (QQLCompiler.KEYWORD_SYMBOL);
    }   
}
