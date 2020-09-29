package deltix.qsrv.snmp.modimpl;

import deltix.qsrv.hf.tickdb.pub.mon.TBLoader;
import deltix.qsrv.snmp.model.timebase.Loader;

/**
 *
 */
public class LoaderImpl implements Loader {
    private int id;
    private TBLoader loader;

    public LoaderImpl(TBLoader loader) {
        this.id = (int) loader.getId();
        this.loader = loader;
    }

    @Override
    public int      getLoaderId() {
        return id;
    }

    @Override
    public String getSource() {
        return loader.getTargetStreamKey();
    }

    @Override
    public String   getLoaderLastMessageTime() {
        return SnmpUtil.formatDateTimeMillis(loader.getLastMessageSysTime());
    }
}
