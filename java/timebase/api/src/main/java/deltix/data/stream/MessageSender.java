package deltix.data.stream;

import deltix.streaming.MessageChannel;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.lang.Disposable;

/**
 * Something that can provide messages to given target channel
 */
public interface MessageSender extends Disposable {
    void send (MessageChannel<InstrumentMessage> target);
}
