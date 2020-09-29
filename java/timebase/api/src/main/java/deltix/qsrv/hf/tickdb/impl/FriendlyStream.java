package deltix.qsrv.hf.tickdb.impl;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.pub.md.RecordClassSet;

/**
 *  Allows the remote layer to directly retrieve the RecordClassSet object
 *  from a TickStreamImpl.
 */
public interface FriendlyStream {

    public RecordClassSet       getMetaData ();

    public boolean              hasWriter(IdentityKey id);

    public void                 addInstrument(IdentityKey id);
}
