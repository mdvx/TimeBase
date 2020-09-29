package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.pub.NullValueException;
import deltix.qsrv.hf.pub.ReadableValue;
import deltix.qsrv.hf.pub.codec.validerrors.ValidationError;
import deltix.util.memory.MemoryDataInput;


/**
 *  Implements sequential access to unbound records.
 */
public interface UnboundDecoder extends ReadableValue {
    public void                 beginRead (MemoryDataInput in);
    
    public RecordClassInfo      getClassInfo (); 
    
    public boolean              nextField ();
    
    public NonStaticFieldInfo   getField ();
    public boolean              previousField ();
    public boolean              seekField (int index);

    public int                  comparePrimaryKeys (
        MemoryDataInput             in1, 
        MemoryDataInput             in2
    );
    
    public int                  compareAll (
        MemoryDataInput             in1, 
        MemoryDataInput             in2
    );

    public ValidationError validate ();
}
