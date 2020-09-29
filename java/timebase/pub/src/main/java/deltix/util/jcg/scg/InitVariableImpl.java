package deltix.util.jcg.scg;

import deltix.util.jcg.JExpr;
import deltix.util.jcg.JInitVariable;
import java.io.IOException;

/**
 *
 */
class InitVariableImpl
    extends VariableImpl
    implements JInitVariable
{
    private JExpr       initValue = null;

    InitVariableImpl(JContextImpl context, int modifiers, String type, String name) {
        super (context, modifiers, type, name);
    }

    public void         printDeclaration (SourceCodePrinter out)
        throws IOException
    {
        out.newLine ();
        printHead (out);

        if (initValue != null)
            out.print (" = ", initValue);

        out.println (";");
    }

    @Override
    public void         setInitValue (JExpr value) {
        initValue = value;
    }
}
