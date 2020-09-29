package deltix.qsrv.hf.pub.codec.validerrors;

import deltix.qsrv.hf.pub.codec.NonStaticFieldInfo;

/**
 *
 */
public final class DecodingError extends ValidationError {
    public final Throwable          exception;
    
    public DecodingError (
        int                         atOffset,        
        NonStaticFieldInfo          fieldInfo,
        Throwable                   exception
    )
    {
        super (atOffset, fieldInfo);
        
        this.exception = exception;
    }        
}
