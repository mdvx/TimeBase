package deltix.qsrv.hf.pub.codec.validerrors;

import deltix.qsrv.hf.pub.codec.NonStaticFieldInfo;

/**
 *
 */
public final class IllegalNullValue extends ValidationError {
    public IllegalNullValue (
        int                         atOffset,        
        NonStaticFieldInfo          fieldInfo
    )
    {
        super (atOffset, fieldInfo);
    }        
}
