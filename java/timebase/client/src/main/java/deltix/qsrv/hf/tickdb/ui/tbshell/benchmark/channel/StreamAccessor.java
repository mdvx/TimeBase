package deltix.qsrv.hf.tickdb.ui.tbshell.benchmark.channel;

import deltix.streaming.MessageChannel;
import deltix.streaming.MessageSource;
import deltix.qsrv.hf.pub.ChannelPerformance;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.LoadingOptions;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.StreamOptions;
import deltix.qsrv.hf.tickdb.pub.StreamScope;
import deltix.qsrv.hf.tickdb.pub.TickCursor;

/**
 * @author Alexei Osipov
 */
public class StreamAccessor implements ChannelAccessor {

    private final ChannelPerformance channelPerformance;
    private final StreamScope scope;

    public StreamAccessor(StreamScope scope, ChannelPerformance channelPerformance) {
        this.scope = scope;
        this.channelPerformance = channelPerformance;
    }

    @Override
    public void createChannel(RemoteTickDB tickDB, String channelKey, RecordClassDescriptor[] rcd) {
        StreamOptions options = new StreamOptions();
        options.scope = this.scope;
        if (rcd.length != 1) {
            options.setPolymorphic(rcd);
        } else {
            options.setFixedType(rcd[0]);
        }
        tickDB.createStream(channelKey, options);
    }

    @Override
    public void deleteChannel(RemoteTickDB tickDB, String channelKey) {
        tickDB.getStream(channelKey).delete();
    }

    @Override
    public MessageChannel<InstrumentMessage> createLoader(RemoteTickDB tickDB, String channelKey) {
        LoadingOptions options = new LoadingOptions();
        options.channelPerformance = this.channelPerformance;
        return tickDB.getStream(channelKey).createLoader(options);
    }

    @Override
    public MessageSource<InstrumentMessage> createConsumer(RemoteTickDB tickDB, String channelKey) {
        SelectionOptions options = new SelectionOptions();
        options.live = true;
        TickCursor cursor = tickDB.getStream(channelKey).createCursor(options);
        cursor.subscribeToAllTypes();
        cursor.subscribeToAllEntities();
        cursor.reset(Long.MIN_VALUE);
        return cursor;
    }
}
