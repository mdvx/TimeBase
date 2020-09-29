package deltix.data.stream;

import deltix.streaming.MessageSource;
import deltix.timebase.messages.TimeStampedMessage;
import deltix.util.concurrent.IntermittentlyAvailableCursor;
import deltix.util.concurrent.NextResult;

import java.util.Comparator;

public class IAMessageSourceMultiplexer<T extends TimeStampedMessage> extends MessageSourceMultiplexer<T> implements IntermittentlyAvailableCursor {

    public IAMessageSourceMultiplexer() {
    }

    public IAMessageSourceMultiplexer(boolean ascending, boolean realTimeNotification, Comparator<T> c) {
        super(ascending, realTimeNotification, c);
    }

    public IAMessageSourceMultiplexer(boolean ascending, boolean realTimeNotification) {
        super(ascending, realTimeNotification);
    }

    @Override
    public NextResult           nextIfAvailable() {
        return syncNext(false);
    }

    @Override
    protected NextResult        moveNext(MessageSource<T> feed, boolean addEmpty) {
        if (feed instanceof IntermittentlyAvailableCursor) {
            NextResult result = ((IntermittentlyAvailableCursor) feed).nextIfAvailable();

            if (result == NextResult.UNAVAILABLE && addEmpty)
                addEmptySource(feed);

            return result;
        }

        return super.moveNext(feed, addEmpty);
    }
}
