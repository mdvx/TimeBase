package deltix.qsrv.hf.tickdb.pub.mapreduce;


import deltix.streaming.MessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.concurrent.UncheckedInterruptedException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public abstract class MessageMapper<K, V> extends Mapper<LongWritable, InstrumentMessage, K, V> {

    public abstract void process(MessageSource<InstrumentMessage> source, Context context)
            throws IOException, InterruptedException;

    @Override
    public final void run(Context context) throws IOException, InterruptedException {
        setup(context);
        try {
            process(new Source(context), context);
        } finally {
            cleanup(context);
        }
    }

    @Override
    protected final void map(LongWritable key, InstrumentMessage value, Context context) throws IOException, InterruptedException {
         throw new UnsupportedOperationException("Map is not implemented");
    }

    private class Source implements MessageSource<InstrumentMessage> {

        private final Context       context;
        private boolean             hasNext;

        public Source(Context context) {
            this.context = context;
        }

        @Override
        public InstrumentMessage getMessage() {
            try {
                return context.getCurrentValue();
            } catch (IOException e) {
                throw new deltix.util.io.UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new UncheckedInterruptedException(e);
            }
        }

        @Override
        public boolean next() {
            try {
                return hasNext = context.nextKeyValue();
            } catch (IOException e) {
                throw new deltix.util.io.UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new UncheckedInterruptedException(e);
            }
        }

        @Override
        public boolean isAtEnd() {
            return hasNext;
        }

        @Override
        public void close() {

        }
    }
}
