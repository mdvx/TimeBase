package deltix.qsrv.hf.tickdb.impl.queue;

import deltix.streaming.MessageChannel;
import deltix.data.stream.MessageEncoder;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.impl.QuickMessageFilter;

/**
 * @author Alexei Osipov
 */
public interface TransientMessageQueue {
    QueueMessageReader getMessageReader(QuickMessageFilter filter, boolean polymorphic, boolean realTimeNotification);

    MessageChannel<InstrumentMessage> getWriter(MessageEncoder<InstrumentMessage> encoder);

    void close();
}
