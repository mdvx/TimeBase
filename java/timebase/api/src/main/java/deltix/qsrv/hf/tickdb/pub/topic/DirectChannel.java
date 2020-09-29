package deltix.qsrv.hf.tickdb.pub.topic;

import deltix.data.stream.ChannelPreferences;
import deltix.data.stream.DXChannel;
import deltix.data.stream.UnknownChannelException;
import deltix.timebase.messages.InstrumentMessage;

import javax.annotation.CheckReturnValue;

/**
 * Wrapper object for Topic API (see {@link TopicDB}).
 *
 * @author Alexei Osipov
 */
public interface DirectChannel extends DXChannel<InstrumentMessage> {

    /**
     * Creates a non-blocking poll-style message consumer.
     * @param preferences consumer preferences
     * @return returns {@link MessagePoller} that can be used to poll messages from subscription.
     */
    @CheckReturnValue
    MessagePoller createPollingConsumer(ChannelPreferences preferences)
            throws UnknownChannelException;
}
