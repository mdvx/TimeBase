package deltix.qsrv.hf.tickdb.web.model.impl;

import deltix.qsrv.hf.tickdb.pub.mon.TBLoader;
import deltix.qsrv.hf.tickdb.web.model.pub.LoadersModel;

/**
 *
 */
public class LoadersModelImpl extends TimeBaseModelImpl implements LoadersModel {

    public String getTitle() {
        return "Loaders";
    }

    @Override
    public TBLoader[] getOpenLoaders() {
        return getMonitor().getOpenLoaders();
    }
}
