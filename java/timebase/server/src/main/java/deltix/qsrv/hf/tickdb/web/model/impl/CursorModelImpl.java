package deltix.qsrv.hf.tickdb.web.model.impl;

import deltix.qsrv.hf.tickdb.pub.mon.TBCursor;
import deltix.qsrv.hf.tickdb.pub.mon.TBObject;
import deltix.qsrv.hf.tickdb.web.model.pub.CursorModel;

/**
 *
 */
public class CursorModelImpl extends TimeBaseModelImpl implements CursorModel {

    private final TBCursor cursor;

    public CursorModelImpl(long id) {
        TBObject tbObject = getMonitor().getObjectById(id);
        if (tbObject instanceof TBCursor)
            cursor = (TBCursor) tbObject;
        else
            cursor = null;
    }

    public String getTitle() {
        return "Cursor " + (cursor != null ? cursor.getId() : "");
    }

    @Override
    public TBCursor getCursor() {
        return cursor;
    }
}
