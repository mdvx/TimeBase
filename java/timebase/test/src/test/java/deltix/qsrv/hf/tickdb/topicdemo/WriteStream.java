package deltix.qsrv.hf.tickdb.topicdemo;

import deltix.streaming.MessageChannel;
import deltix.qsrv.hf.pub.ChannelPerformance;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.LoadingOptions;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.topicdemo.util.DemoConf;

/**
 * @author Alexei Osipov
 */
public class WriteStream extends WriteBase {
    private final ChannelPerformance channelPerformance;

    public WriteStream(ChannelPerformance channelPerformance) {
        this.channelPerformance = channelPerformance;
    }

    @Override
    protected MessageChannel<InstrumentMessage> createLoader(RemoteTickDB client) {
        LoadingOptions options = new LoadingOptions();
        options.channelPerformance = this.channelPerformance;
        return client.getStream(DemoConf.DEMO_MAIN_STREAM).createLoader(options);
    }
}
