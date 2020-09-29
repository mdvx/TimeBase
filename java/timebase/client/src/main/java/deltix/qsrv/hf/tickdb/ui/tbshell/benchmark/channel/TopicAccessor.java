package deltix.qsrv.hf.tickdb.ui.tbshell.benchmark.channel;

import deltix.streaming.MessageChannel;
import deltix.streaming.MessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.util.io.idlestrat.BusySpinIdleStrategy;

/**
 * @author Alexei Osipov
 */
public class TopicAccessor implements ChannelAccessor {
    @Override
    public void createChannel(RemoteTickDB tickDB, String channelKey, RecordClassDescriptor[] rcd) {
        tickDB.getTopicDB().createTopic(channelKey, rcd, null);
    }

    @Override
    public void deleteChannel(RemoteTickDB tickDB, String channelKey) {
        tickDB.getTopicDB().deleteTopic(channelKey);
    }

    @Override
    public MessageChannel<InstrumentMessage> createLoader(RemoteTickDB tickDB, String channelKey) {
        return tickDB.getTopicDB().createPublisher(channelKey, null, new BusySpinIdleStrategy());
    }

    @Override
    public MessageSource<InstrumentMessage> createConsumer(RemoteTickDB tickDB, String channelKey) {
        return tickDB.getTopicDB().createConsumer(channelKey, null, new BusySpinIdleStrategy());
    }
}
