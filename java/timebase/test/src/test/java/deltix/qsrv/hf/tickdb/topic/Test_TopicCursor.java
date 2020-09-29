package deltix.qsrv.hf.tickdb.topic;

import deltix.streaming.MessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.topic.TopicDB;
import deltix.util.JUnitCategories;
import deltix.util.concurrent.CursorIsClosedException;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alexei Osipov
 */
@Category(JUnitCategories.TickDB.class)
public class Test_TopicCursor extends BaseTimeBaseTopicReadingTest {
    private MessageSource<InstrumentMessage> messageSource;

    @Test(timeout = TEST_TIMEOUT)
    public void test() throws Exception {
        executeTest();
    }

    @NotNull
    protected Runnable createReader(AtomicLong messagesReceivedCounter, MessageValidator messageValidator, String topicKey, TopicDB topicDB) {

        this.messageSource = topicDB.createConsumer(topicKey, null, null);

        return () -> {
            RatePrinter ratePrinter = new RatePrinter("Reader");
            ratePrinter.start();
            try {
                while (messageSource.next()) {
                    messageValidator.validate(messageSource.getMessage());
                    ratePrinter.inc();
                    messagesReceivedCounter.incrementAndGet();
                }
            } catch (CursorIsClosedException ignore) {
            }

            messageSource.close();
        };
    }

    protected void stopReader() {
        messageSource.close();
    }

}