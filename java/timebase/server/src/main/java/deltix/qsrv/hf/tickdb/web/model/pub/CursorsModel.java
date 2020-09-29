package deltix.qsrv.hf.tickdb.web.model.pub;

import deltix.qsrv.hf.tickdb.pub.mon.TBCursor;

/**
 *
 */
public interface CursorsModel extends TimeBaseModel {

    TBCursor[]          getOpenCursors();

}
