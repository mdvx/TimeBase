package deltix.qsrv.hf.tickdb.web.model.pub;

import deltix.qsrv.hf.tickdb.pub.mon.TBLock;

/**
 *
 */
public interface LocksModel extends TimeBaseModel {

    TBLock[]            getLocks();

}
