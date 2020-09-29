package deltix.qsrv.hf.tickdb.tests.reports;

import deltix.util.collections.generated.LongArrayList;

import java.util.Collection;

public interface TimestampedMetric<T> {

    TimestampedMetric<T> addValue(long timestamp, T value);

    String valueToString(T value);

    Collection<T> values();

    LongArrayList timestamps();
}
