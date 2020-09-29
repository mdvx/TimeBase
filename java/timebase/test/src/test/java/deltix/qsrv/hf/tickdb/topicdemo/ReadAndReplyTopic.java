package deltix.qsrv.hf.tickdb.topicdemo;

import deltix.streaming.MessageChannel;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.pub.topic.MessagePoller;
import deltix.qsrv.hf.tickdb.pub.topic.MessageProcessor;
import deltix.qsrv.hf.tickdb.topicdemo.util.DemoConf;
import deltix.util.io.idlestrat.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;

import java.util.concurrent.CountDownLatch;

/**
 * @author Alexei Osipov
 */
public class ReadAndReplyTopic extends ReadAndReplyBase {

    @Override
    MessageChannel<InstrumentMessage> createReplyLoader(RemoteTickDB client) {
        return client.getTopicDB().createPublisher(DemoConf.DEMO_ECHO_TOPIC, null, new BusySpinIdleStrategy());
    }

    @Override
    void work(RemoteTickDB client, CountDownLatch stopSignal, MessageProcessor messageProcessor) {
        MessagePoller messagePoller = client.getTopicDB().createPollingConsumer(DemoConf.DEMO_MAIN_TOPIC, null);

        IdleStrategy idleStrategy = DemoConf.getReaderIdleStrategy();

        // Process messages from topic until stop signal triggered
        while (stopSignal.getCount() > 0) {
            idleStrategy.idle(messagePoller.processMessages(100, messageProcessor));
        }
        messagePoller.close();
    }
}
