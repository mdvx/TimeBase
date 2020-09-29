package deltix.qsrv.hf.tickdb.topicdemo;

import deltix.streaming.MessageChannel;
import deltix.qsrv.hf.pub.ChannelPerformance;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.LoadingOptions;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.TickCursor;
import deltix.qsrv.hf.tickdb.pub.topic.MessageProcessor;
import deltix.qsrv.hf.tickdb.topicdemo.util.DemoConf;

import java.util.concurrent.CountDownLatch;

/**
 * @author Alexei Osipov
 */
public class ReadAndReplyStream extends ReadAndReplyBase {
    private final ChannelPerformance channelPerformance;

    public ReadAndReplyStream(ChannelPerformance channelPerformance) {
        this.channelPerformance = channelPerformance;
    }

    @Override
    MessageChannel<InstrumentMessage> createReplyLoader(RemoteTickDB client) {
        LoadingOptions options = new LoadingOptions();
        options.channelPerformance = this.channelPerformance;
        return client.getStream(DemoConf.DEMO_ECHO_STREAM).createLoader(options);
    }

    @Override
    void work(RemoteTickDB client, CountDownLatch stopSignal, MessageProcessor messageProcessor) {
        SelectionOptions options = new SelectionOptions();
        options.channelPerformance = this.channelPerformance;
        options.live = true;
        TickCursor cursor = client.getStream(DemoConf.DEMO_MAIN_STREAM).createCursor(options);
        cursor.subscribeToAllEntities();
        cursor.subscribeToAllTypes();
        cursor.reset(Long.MIN_VALUE);

        // Process messages from topic until stop signal triggered
        while (stopSignal.getCount() > 0 && cursor.next()) {
            messageProcessor.process(cursor.getMessage());
        }
        cursor.close();
    }
}
