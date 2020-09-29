package deltix.qsrv.hf.tickdb.impl;

import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.codec.RecordClassInfo;
import deltix.qsrv.hf.pub.codec.validerrors.ValidationError;

/**
 *  Implements sequential access to unbound records.
 */
public interface MessageValidator {
    public RecordClassInfo getClassInfo ();
    
    public int                  validate (RawMessage msg);
    
    public int                  getNumErrors ();
    
    public ValidationError      getError (int idx);
}
