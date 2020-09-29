package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.pub.WritableValue;

/**
 *
 */
public interface UnboundEncoder extends WritableValue {
    public boolean              nextField ();
    
    public NonStaticFieldInfo   getField ();

    /**
     * Checks that NOT NULLABLE restriction was not violated (Optional).
     */
    public void endWrite();
}
