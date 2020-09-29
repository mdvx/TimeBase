package deltix.qsrv.hf.tickdb.perfomancedrop.stubs;

import deltix.streaming.MessageSource;
import deltix.timebase.messages.InstrumentMessage;

import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.md.FloatDataType;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.StreamConfigurationHelper;
import deltix.qsrv.hf.tickdb.pub.query.TypedMessageSource;
import deltix.util.lang.DisposableListener;
import org.apache.commons.lang.NotImplementedException;

import java.util.concurrent.TimeUnit;

/**
 * Special version of {@link MessageSource} for performance testing purposes.
 */
public class StubMessageSource<T extends InstrumentMessage> implements MessageSource<T>, TypedMessageSource {

    private DisposableListener listener;

    private final InstrumentMessage message;

    public StubMessageSource() {
        message = createRawMessage2();
    }

    @Override
    public T getMessage() {
        return (T) message;
    }

    @Override
    public boolean next() {
        return true;
    }

    @Override
    public boolean isAtEnd() {
        return false;
    }

    @Override
    public void close() {
        // disposed event notification
        if (listener != null)
            listener.disposed(this);
    }

    public void setAvailabilityListener(Runnable maybeAvailable) {

    }

    public long                 getStartTimestamp() {
        return System.currentTimeMillis();
    }

    public long                 getEndTimestamp() {
        return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10);
    }


    public void                 setDisposableListener(DisposableListener<StubMessageSource> listener) {
        this.listener = listener;
    }

    private static RawMessage createRawMessage() {

        RecordClassDescriptor mmDescriptor = StreamConfigurationHelper.mkMarketMessageDescriptor(null, false);
        RecordClassDescriptor tradeDescriptor = StreamConfigurationHelper.mkTradeMessageDescriptor(
                mmDescriptor, null, null, FloatDataType.ENCODING_FIXED_DOUBLE, FloatDataType.ENCODING_FIXED_DOUBLE);

        RawMessage msg = new RawMessage();
        msg.type = tradeDescriptor;
        msg.data = new byte[10];
        msg.setSymbol("AAA");
        return msg;
    }

    private static InstrumentMessage createRawMessage2() {

        RecordClassDescriptor mmDescriptor = StreamConfigurationHelper.mkMarketMessageDescriptor(null, false);
        RecordClassDescriptor tradeDescriptor = StreamConfigurationHelper.mkTradeMessageDescriptor(
                mmDescriptor, null, null, FloatDataType.ENCODING_FIXED_DOUBLE, FloatDataType.ENCODING_FIXED_DOUBLE);

        RawMessage msg = new RawMessage();
        msg.type = tradeDescriptor;
        msg.data = new byte[128];
        msg.setSymbol("X");
        msg.offset = 0;
        msg.length = 50;
        msg.setNanoTime(System.nanoTime());
        return msg;
    }

    @Override
    public int getCurrentTypeIndex() {
        throw new NotImplementedException();
    }

    @Override
    public RecordClassDescriptor getCurrentType() {
        throw new NotImplementedException();
    }
}

