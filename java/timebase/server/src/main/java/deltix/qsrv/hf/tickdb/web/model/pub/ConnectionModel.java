package deltix.qsrv.hf.tickdb.web.model.pub;

import deltix.util.vsocket.VSDispatcher;

/**
 *
 */
public interface ConnectionModel extends TimeBaseModel {

    VSDispatcher        getDispatcher();

}
