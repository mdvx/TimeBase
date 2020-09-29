package deltix.qsrv.hf.tickdb.impl;

import deltix.qsrv.hf.tickdb.impl.mon.TBMonitorImpl;
import deltix.qsrv.hf.tickdb.pub.lock.LockType;
import deltix.qsrv.hf.tickdb.pub.mon.TBLock;
import deltix.util.lang.DisposableListener;
import deltix.util.vsocket.VSDispatcher;
import deltix.util.vsocket.VSServerFramework;

class StreamLockImpl extends ServerLock implements TBLock {
    private final ServerStreamImpl  stream;

    private final long monId;
    private String user;
    private String application;

    StreamLockImpl(ServerStreamImpl stream, ServerLock lock) {
        super(lock.getType(), lock.getGuid(), lock.getClientId());
        this.stream = stream;
        this.monId = getMonId();
    }

    StreamLockImpl(ServerStreamImpl stream, LockType type, String guid) {
        super(type, guid);
        this.stream = stream;
        this.monId = getMonId();
    }

    private long getMonId() {
        return (stream.getDB() instanceof TBMonitorImpl) ?
                ((TBMonitorImpl) stream.getDB()).getLockId(this) : TBMonitorImpl.UNKNOWN_ID;
    }

    @Override
    public boolean      isValid() {
        return stream.hasLock(this);
    }

    @Override
    public void         release() {
        stream.removeLock(this);
    }

    @Override
    public void         setClientId(String clientId) {
        VSDispatcher dispatcher = VSServerFramework.INSTANCE.getDispatcher(clientId);

        // in case of HTTP requests dispatcher == null
        if (dispatcher != null && stream instanceof DisposableListener)
            dispatcher.addDisposableListener((DisposableListener)stream);

        super.setClientId(clientId);
    }

    @Override
    public long getId() {
        return monId;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public String getStreamKey() {
        return stream.getKey();
    }

    @Override
    public String getHost() {
        return getHost(clientId);
    }

    @Override
    public String toString() {
        return "Lock [" + stream.getKey() + ", " + getType() + ", " + getGuid() + "] from " + clientId;
    }
}
