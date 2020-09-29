package deltix.samples.timebase.hdfs;

import deltix.streaming.MessageSource;
import deltix.qsrv.hf.blocks.InstrumentToObjectMap;
import deltix.timebase.api.messages.IdentityKey;
import deltix.timebase.api.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.mapreduce.MessageMapper;
import deltix.qsrv.hf.tickdb.pub.mapreduce.MessageReducer;
import deltix.qsrv.hf.tickdb.pub.mapreduce.WritableInstrument;
import org.apache.hadoop.io.LongWritable;


import java.io.IOException;
import java.util.Map;

public class InstrumentsCounter  {

    public static class Mapper extends MessageMapper<WritableInstrument, LongWritable> {

        private final InstrumentToObjectMap<LongWritable> counters = new InstrumentToObjectMap<LongWritable>();

        @Override
        public void process(MessageSource<InstrumentMessage> source, Context context) throws IOException, InterruptedException {
            while (source.next()) {
                InstrumentMessage value = source.getMessage();

                LongWritable writable = counters.get(value);
                if (writable == null)
                    counters.put(value, writable = new LongWritable(1));
                else
                    writable.set(writable.get() + 1);
            }

            for (Map.Entry<IdentityKey, LongWritable> entry : counters.entrySet())
                context.write(new WritableInstrument(entry.getKey().getInstrumentType(), entry.getKey().getSymbol()), entry.getValue());
        }
    }

    public static class Reducer extends MessageReducer<WritableInstrument, LongWritable> {

        public Reducer() {
        }

        @Override
        protected void reduce(WritableInstrument key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            long counter = 0;
            for (LongWritable value : values)
                counter += value.get();

            //context.write(key, new LongWritable(counter));
        }
    }


}
