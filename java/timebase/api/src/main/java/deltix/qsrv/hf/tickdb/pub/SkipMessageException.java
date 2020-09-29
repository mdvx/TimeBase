package deltix.qsrv.hf.tickdb.pub;

import deltix.timebase.messages.InstrumentMessage;
import deltix.util.time.GMT;

/**
 * Notifies a client that an out-of-sequence message was skipped.
 */
public class SkipMessageException extends LoadingMessageException {
    private String stream;

    public SkipMessageException(InstrumentMessage msg, String stream) {
        super(msg);
        this.stream = stream;
    }

    @Override
    public String getMessage() {
        return String.format("[%s] Message %s %s GMT is skipped",
                stream, symbol, GMT.formatNanos(nanoTime));
    }
}
