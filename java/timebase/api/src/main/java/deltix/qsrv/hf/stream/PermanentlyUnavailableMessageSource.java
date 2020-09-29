package deltix.qsrv.hf.stream;

import deltix.data.stream.RealTimeMessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.concurrent.IntermittentlyAvailableResource;
import deltix.util.concurrent.UnavailableResourceException;

/**
 * Always throws UnavailableResourceException on next();
 */
public class PermanentlyUnavailableMessageSource implements RealTimeMessageSource<InstrumentMessage>, IntermittentlyAvailableResource {

    @Override
    public void setAvailabilityListener(Runnable listener) {
        // we will never call back
    }

    @Override
    public InstrumentMessage getMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean next() {
        throw UnavailableResourceException.INSTANCE;
    }

    @Override
    public boolean isAtEnd() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isRealTime() {
        return true;
    }

    @Override
    public boolean realTimeAvailable() {
        return true;
    }
}
