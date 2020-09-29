package deltix.qsrv.hf.tickdb.impl;

import deltix.qsrv.dtb.store.pub.EntityFilter;
import deltix.qsrv.hf.tickdb.StreamConfigurationHelper;
import deltix.timebase.messages.InstrumentMessage;

import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.md.FloatDataType;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.TickStream;
import deltix.timebase.api.messages.BestBidOfferMessage;
import deltix.util.lang.DisposableListener;
import org.apache.commons.lang3.NotImplementedException;

import java.util.concurrent.TimeUnit;

/**
 * Special version of {@link TickStreamReader} for performance testing purposes.
 * It's placed in this package to have access to {@link TickStreamReader}.
 */
public class StubTickStreamReader implements TickStreamReader {

    private DisposableListener<StubTickStreamReader> listener;

    private final InstrumentMessage message;

    public StubTickStreamReader() {
        message = createRawMessage2();
    }

    @Override
    public void         reset(long timestamp) {

    }

    @Override
    public boolean isRealTime() {
        return false;
    }

    @Override
    public boolean realTimeAvailable() {
        return false;
    }

    @Override
    public InstrumentMessage getMessage() {
        return message;
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

    public void                 setFilter(EntityFilter filter) {

    }

    @Override
    public TickStream getStream() {
        throw new NotImplementedException("");
    }

    @Override
    public int getCurrentTypeIndex() {
        throw new NotImplementedException("");
    }

    @Override
    public RecordClassDescriptor getCurrentType() {
        throw new NotImplementedException("");
    }

    public void                 setLimitTimestamp(long timestamp) {
        throw new NotImplementedException("");
    }

    public void                 setDisposableListener(DisposableListener<StubTickStreamReader> listener) {
        this.listener = listener;
    }

    private static RawMessage createRawMessage() {
        BestBidOfferMessage bestBidOfferMessage = new BestBidOfferMessage();

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
}

