package deltix.qsrv.hf.tickdb.ui.tbshell.benchmark.channel;

import deltix.streaming.MessageChannel;
import deltix.streaming.MessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;

/**
 * @author Alexei Osipov
 */
public interface ChannelAccessor {
    void createChannel(RemoteTickDB tickDB, String channelKey, RecordClassDescriptor[] rcd);
    void deleteChannel(RemoteTickDB tickDB, String channelKey);

    MessageChannel<InstrumentMessage> createLoader(RemoteTickDB tickDB, String channelKey);

    MessageSource<InstrumentMessage> createConsumer(RemoteTickDB tickDB, String channelKey);
}
