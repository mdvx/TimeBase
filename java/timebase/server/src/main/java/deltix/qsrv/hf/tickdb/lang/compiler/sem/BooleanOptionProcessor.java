package deltix.qsrv.hf.tickdb.lang.compiler.sem;

import deltix.qsrv.hf.pub.md.StandardTypes;
import deltix.qsrv.hf.tickdb.lang.compiler.sx.CompiledConstant;
import deltix.qsrv.hf.tickdb.lang.errors.IllegalOptionValueException;
import deltix.qsrv.hf.tickdb.lang.pub.OptionElement;

/**
 *
 */
public abstract class BooleanOptionProcessor <T> extends OptionProcessor <T> {
    public BooleanOptionProcessor (
        String      key
    )
    {
        super (key, StandardTypes.CLEAN_BOOLEAN);        
    }
    
    protected abstract void     set (T target, boolean value);
    
    protected abstract boolean  get (T source);
    
    @Override
    public final void           process (OptionElement option, CompiledConstant value, T target) {
        if (value == null || value.isNull ())
            throw new IllegalOptionValueException (option, null);
            
        set (target, value.getBoolean ());
    }

    @Override
    protected void              printValue (T source, StringBuilder out) {
        out.append (get (source) ? "TRUE" : "FALSE");
    }        
}
