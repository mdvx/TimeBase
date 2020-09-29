package deltix.qsrv.hf.tickdb.impl.topic.topicregistry;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.impl.topic.CommunicationPipe;
import deltix.qsrv.hf.tickdb.pub.Messages;
import deltix.qsrv.hf.tickdb.pub.topic.MessageProcessor;
import deltix.qsrv.hf.tickdb.pub.topic.settings.TopicType;
import deltix.qsrv.hf.topic.consumer.DirectReaderFactory;
import deltix.qsrv.hf.topic.consumer.SubscriptionWorker;
import deltix.qsrv.hf.topic.loader.DirectLoaderFactory;
import deltix.streaming.MessageChannel;
import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.InstrumentMessage;
import deltix.timebase.messages.service.ErrorMessage;
import deltix.timebase.messages.service.EventMessage;
import deltix.util.concurrent.QuickExecutor;
import deltix.util.time.TimeKeeper;
import io.aeron.Aeron;
import org.junit.Test;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Note: this test requires running Aeron driver (see deltix.util.io.aeron.AeronDriver).
 * TODO: Make this test independent of external driver.
 *
 * Stress test. Does not stops itself.
 *
 * @author Alexei Osipov
 */
public class DirectChannelWithServerTest {
    static {
        System.setProperty("deltix.qsrv.hf.RatePrinter.enabled", "true");
    }

    @Test
    public void run() throws Exception {
        TopicTestUtils.initTempQSHome();

        Aeron aeron = TopicTestUtils.createAeron();

        DirectTopicRegistry directTopicRegistry = new DirectTopicRegistry();
        List<RecordClassDescriptor> types = Collections.singletonList(Messages.ERROR_MESSAGE_DESCRIPTOR);

        String topicName = "testTopic";
        AtomicInteger streamIdGenerator = new AtomicInteger(new Random().nextInt());

        ConstantIdentityKey key = new ConstantIdentityKey("ABC");
        directTopicRegistry.createDirectTopic(topicName, types, null, streamIdGenerator::incrementAndGet, Collections.singletonList(key), TopicType.IPC, null, null);

        CommunicationPipe pipe = new CommunicationPipe();


        InputStream loaderInputStream = pipe.getInputStream();
        List<ConstantIdentityKey> keys = Collections.emptyList();
        LoaderSubscriptionResult loaderSubscriptionResult = directTopicRegistry.addLoader(topicName, loaderInputStream, keys, QuickExecutor.createNewInstance("Test Executor", null), aeron, true, null);
        Runnable dataAvailabilityCallback = loaderSubscriptionResult.getDataAvailabilityCallback();

        OutputStream outputStream = new FilterOutputStream(pipe.getOutputStream()) {
            @Override
            public void flush() throws IOException {
                super.flush();
                dataAvailabilityCallback.run();
            }
        };

        Thread loaderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                List<ConstantIdentityKey> mapping = Arrays.asList(loaderSubscriptionResult.getMapping());
                MessageChannel<InstrumentMessage> channel = new DirectLoaderFactory().create(aeron, false, loaderSubscriptionResult.getPublisherChannel(), loaderSubscriptionResult.getMetadataSubscriberChannel(), loaderSubscriptionResult.getDataStreamId(), loaderSubscriptionResult.getServerMetadataStreamId(), loaderSubscriptionResult.getTypes(), loaderSubscriptionResult.getLoaderNumber(), outputStream, mapping, null, null);

                ErrorMessage msg = new ErrorMessage();
                msg.setSymbol("ABC");
                msg.setDetails("more to come");

                while (true) {
                    msg.setTimeStampMs(TimeKeeper.currentTime);
                    channel.send(msg);
                }
            }
        });
        loaderThread.setName("SENDER");
        loaderThread.start();

        ReaderSubscriptionResult readerSubscriptionResult = directTopicRegistry.addReader(topicName, true, null, null);

        RatePrinter ratePrinter = new RatePrinter("Listener");
        SubscriptionWorker directMessageListener = new DirectReaderFactory().createListener(aeron, false, readerSubscriptionResult.getSubscriberChannel(), readerSubscriptionResult.getDataStreamId(), readerSubscriptionResult.getTypes(), new MessageProcessor() {
            @Override
            public void process(InstrumentMessage message) {
                ratePrinter.inc();
            }
        }, null, directTopicRegistry.getMappingProvider(topicName));
        ratePrinter.start();
        directMessageListener.processMessagesUntilStopped();
    }
}