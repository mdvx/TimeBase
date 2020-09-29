package deltix.qsrv.hf.tickdb.web.model.pub;

import deltix.util.vsocket.VSServerFramework;

/**
 *
 */
public interface ConnectionsModel extends TimeBaseModel {

    VSServerFramework getServerFramework();

}
