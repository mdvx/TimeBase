package deltix.samples.timebase.hdfs;

import deltix.streaming.MessageSource;
import deltix.qsrv.hf.blocks.BarGenerator;
import deltix.timebase.api.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.mapreduce.*;
import deltix.util.time.Interval;
import org.apache.hadoop.io.LongWritable;

import java.io.IOException;

/**
 *
 */
public class BarAggregator {
    public static final String INTERVAL = "bar.aggregator.mapper.interval";
    public static final String INTERVAL_DEF = "1D";

    public static class Mapper extends MessageMapper<LongWritable, WritableInstrumentMessage> {
        @Override
        public void process(final MessageSource<InstrumentMessage> source, final Context context) throws IOException, InterruptedException {
            BarGenerator barGenerator = new BarGenerator(
                Interval.valueOf(context.getConfiguration().get(INTERVAL, INTERVAL_DEF)),
                new WritableMessageChannel<>(new WritableInstrumentMessage(), context)
            );

            while (source.next())
                barGenerator.process(source.getMessage());
            barGenerator.flushAll();
        }
    }

    public static class Reducer extends MessageReducer<LongWritable, WritableInstrumentMessage> {
        private BarGenerator barGenerator = null;

        @Override
        protected void reduce(LongWritable key, Iterable<WritableInstrumentMessage> values, final Context context) throws IOException, InterruptedException {
            if (barGenerator == null)
                barGenerator = new BarGenerator(
                    Interval.valueOf(context.getConfiguration().get(INTERVAL, INTERVAL_DEF)),
                    new OutputMessageChannel(context));

            for (WritableInstrumentMessage value : values)
                barGenerator.process(value.get(context));
            barGenerator.flushAll();
        }
    }

}
