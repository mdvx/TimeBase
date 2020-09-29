package deltix.qsrv.util.time;

import deltix.qsrv.hf.pub.TimeSource;

/** System real-time clock */
public final class RealTimeSource implements TimeSource {
    public static final RealTimeSource INSTANCE = new RealTimeSource(); 

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
