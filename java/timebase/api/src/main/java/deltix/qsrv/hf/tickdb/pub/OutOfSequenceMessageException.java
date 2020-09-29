package deltix.qsrv.hf.tickdb.pub;

import deltix.timebase.messages.InstrumentMessage;
import deltix.util.time.GMT;

/**
 * Notifies a client that an out-of-sequence message cannot be stored to Timebase.
 */
public class OutOfSequenceMessageException extends LoadingMessageException {
    private String stream;
    private final long lastWrittenNanos;

    public OutOfSequenceMessageException(InstrumentMessage msg, String stream, long lastWrittenNanos) {
        super(msg);
        this.stream = stream;
        this.lastWrittenNanos = lastWrittenNanos;
    }

    public OutOfSequenceMessageException(InstrumentMessage msg, long nstime, String stream, long lastWrittenNanos) {
        super(msg);
        this.nanoTime = nstime;
        this.stream = stream;
        this.lastWrittenNanos = lastWrittenNanos;
    }

    @Override
    public String getMessage() {
        return String.format("[%s] Message %s %s GMT is out of sequence (< %s)",
                stream, symbol, GMT.formatNanos(nanoTime), GMT.formatNanos(lastWrittenNanos));
    }
}
