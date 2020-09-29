package deltix.qsrv.hf.tickdb.pub.mon;

import deltix.qsrv.hf.tickdb.pub.lock.LockType;

/**
 *
 */
public interface TBLock extends TBObject {

    String          getGuid();

    LockType        getType();

    String          getClientId();

    String          getStreamKey();

    String          getHost();
}
