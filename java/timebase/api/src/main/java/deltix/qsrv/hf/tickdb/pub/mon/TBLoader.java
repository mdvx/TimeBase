package deltix.qsrv.hf.tickdb.pub.mon;

import deltix.qsrv.hf.tickdb.pub.LoadingOptions;

/**
 *
 */
public interface TBLoader extends TBChannel, TBObject {
    public LoadingOptions       getOptions ();

    public String               getTargetStreamKey ();

    public double               getProgress();
}
