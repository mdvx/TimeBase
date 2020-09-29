package deltix.qsrv.hf.tickdb.web.model.impl;

import deltix.qsrv.hf.tickdb.pub.mon.TBCursor;
import deltix.qsrv.hf.tickdb.web.model.pub.CursorsModel;

/**
 *
 */
public class CursorsModelImpl extends TimeBaseModelImpl implements CursorsModel {

    public String getTitle() {
        return "Cursors";
    }

    @Override
    public TBCursor[] getOpenCursors() {
        return getMonitor().getOpenCursors();
    }
}
