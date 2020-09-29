package deltix.qsrv.hf.tickdb.web.model.impl;

import deltix.qsrv.hf.tickdb.pub.mon.TBLock;
import deltix.qsrv.hf.tickdb.web.model.pub.LocksModel;

/**
 *
 */
public class LocksModelImpl extends TimeBaseModelImpl implements LocksModel {

    public String getTitle() {
        return "Locks";
    }

    @Override
    public TBLock[] getLocks() {
        return getMonitor().getLocks();
    }
}
