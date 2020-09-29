package deltix.samples.timebase.advanced;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import deltix.streaming.MessageChannel;
import deltix.streaming.MessageSource;
import deltix.timebase.api.messages.InstrumentMessage;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.pub.StreamConfigurationHelper;
import deltix.qsrv.hf.tickdb.pub.TickDBFactory;
import deltix.qsrv.hf.tickdb.pub.topic.DirectChannel;
import deltix.qsrv.hf.tickdb.pub.topic.MessagePoller;
import deltix.qsrv.hf.tickdb.pub.topic.MessageProcessor;
import deltix.qsrv.hf.tickdb.pub.topic.exception.DuplicateTopicException;
import deltix.timebase.api.messages.TradeMessage;
import deltix.util.concurrent.CursorIsClosedException;
import deltix.util.io.idlestrat.BusySpinIdleStrategy;
import deltix.util.io.idlestrat.IdleStrategy;
import deltix.util.lang.Disposable;
import deltix.util.time.TimeKeeper;

import java.util.concurrent.ThreadFactory;

public class LiveChannelSample {

    private static String KEY = "live";

    static volatile boolean running = true;

    public static  class Processor implements MessageProcessor {
        int messageCount = 0;

        @Override
        public void process(InstrumentMessage message) {
            if (message.getSymbol().equals("GOOG")) {
                messageCount += 1;
            }
        }
    }

    public static void createChannel(RemoteTickDB db) {

        RecordClassDescriptor rcd = StreamConfigurationHelper.mkUniversalTradeMessageDescriptor();

        try {
            DirectChannel channel = db.getTopicDB().getTopic(KEY);
            if (channel == null)
                db.getTopicDB().createTopic(KEY, new RecordClassDescriptor[] { rcd }, null);
        } catch (DuplicateTopicException ignore) {
            System.out.println("Topic already exists");
        }
    }

    private static void runPublisher(RemoteTickDB db) {

        // Create publisher
        MessageChannel<InstrumentMessage> channel = db.getTopicDB().createPublisher(KEY, null, new BusySpinIdleStrategy());

        // Create message to reuse
        TradeMessage msg = new TradeMessage();
        msg.setSymbol("GOOG");

        msg.setTimeStampMs(TimeKeeper.currentTime);

        int count = 0;

        while (running) {
            // Update only active message properties
            msg.setSize(100);
            msg.setPrice(959.98);

            channel.send(msg);

            // sleep each 100 message
            if (count++ % 100 == 0)
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
        }

        System.out.println("Published " + count + " messages.");
    }

    private static void runConsumer(RemoteTickDB client) {
        Processor processor = new Processor();

        MessagePoller poller = client.getTopicDB().createPollingConsumer(KEY, null);
        IdleStrategy idleStrategy = new BusySpinIdleStrategy();
        while (running) {
            idleStrategy.idle(poller.processMessages(100, processor));
        }

        System.out.println("Recieved messages: " + processor.messageCount);
    }

    private static void runConsumerAsMessageSource(RemoteTickDB client) {
        MessageSource<InstrumentMessage> source = client.getTopicDB().createConsumer(KEY, null, new BusySpinIdleStrategy());

        int messageCount = 0;
        try {
            while (running) {
                if (source.next()) {
                    InstrumentMessage message = source.getMessage();
                    if (message.getSymbol().equals("GOOG"))
                        messageCount += 1;
                } else {
                    break;
                }
            }
        } catch (CursorIsClosedException e) {
            // in case when channel is closed
        }

        System.out.println("Recieved messages: " + messageCount);
    }

    private static Disposable startConsumerAsWorker(RemoteTickDB client) throws InterruptedException {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().build();

        Processor processor = new Processor();
        return client.getTopicDB().createConsumerWorker(KEY, null, new BusySpinIdleStrategy(), threadFactory, processor);
    }


    public static void main(String[] args) throws InterruptedException {
        // Note: you need a running Timebase server for this test
        try (RemoteTickDB db = TickDBFactory.connect("localhost", 8022, false)) {
            db.open(false);

            createChannel(db);

            new Thread(() -> runPublisher(db)).start();
            new Thread(() -> runConsumer(db)).start();
            new Thread(() -> runConsumerAsMessageSource(db)).start();

            Disposable worker = startConsumerAsWorker(db);

            // let the system run for 10 seconds
            Thread.sleep(10000);
            running = false;

            worker.close();

        }
    }
}
