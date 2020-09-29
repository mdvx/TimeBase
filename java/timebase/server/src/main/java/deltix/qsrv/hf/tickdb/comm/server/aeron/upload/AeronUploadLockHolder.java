package deltix.qsrv.hf.tickdb.comm.server.aeron.upload;

import deltix.qsrv.hf.tickdb.impl.ServerLock;
import deltix.qsrv.hf.tickdb.pub.lock.DBLock;
import deltix.qsrv.hf.tickdb.pub.lock.LockEventListener;
import deltix.qsrv.hf.tickdb.pub.lock.LockHandler;
import deltix.util.lang.Disposable;

/**
 * @author Alexei Osipov
 */
public class AeronUploadLockHolder implements LockEventListener, Disposable {

    private volatile ServerLock lock = null;
    private final LockHandler lockHandler;

    public AeronUploadLockHolder(LockHandler lockHandler) {
        this.lockHandler = lockHandler;
    }

    @Override
    public void         lockAdded(DBLock lock) {
        if (this.lock == null && lock instanceof ServerLock) {
            this.lock = (ServerLock) lock;
        }
    }

    @Override
    public void         lockRemoved(DBLock lock) {
    }

    public ServerLock getLock() {
        return lock;
    }

    @Override
    public void close() {
        if (lockHandler != null) {
            lockHandler.removeEventListener(this);
        }
    }
}
