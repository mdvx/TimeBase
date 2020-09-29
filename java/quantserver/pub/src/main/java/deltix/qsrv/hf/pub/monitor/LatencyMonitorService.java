package deltix.qsrv.hf.pub.monitor;

import deltix.util.lang.Disposable;

public interface LatencyMonitorService extends Disposable {

    void measure(LatencyMetric metric, long signalId, long measureTimeNanos, long latencyNanos, String exchange, String moduleKey);
}
