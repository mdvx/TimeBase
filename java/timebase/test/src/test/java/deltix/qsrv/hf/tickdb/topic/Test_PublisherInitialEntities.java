package deltix.qsrv.hf.tickdb.topic;

import deltix.streaming.MessageChannel;
import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.IdentityKey;
import deltix.timebase.messages.InstrumentMessage;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.TDBTestBase;
import deltix.qsrv.hf.tickdb.TestTopicsStandalone;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.pub.topic.PublisherPreferences;
import deltix.qsrv.hf.tickdb.pub.topic.exception.TopicNotFoundException;
import deltix.util.JUnitCategories.TickDBFast;
import deltix.util.io.idlestrat.YieldingIdleStrategy;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alexei Osipov
 */
@Category(TickDBFast.class)
public class Test_PublisherInitialEntities extends TDBTestBase {


    public Test_PublisherInitialEntities() {
        super(true);
    }

    @Test(timeout = 30_000)
    public void testBlock () {
        RemoteTickDB db = (RemoteTickDB) getTickDb();

        String topicKey = "Test_PublisherInitialEntities";
        try {
            db.getTopicDB().deleteTopic(topicKey);
        } catch (TopicNotFoundException ignored) {
        }

        db.getTopicDB().createTopic(topicKey, new RecordClassDescriptor[]{TestTopicsStandalone.makeTradeMessageDescriptor()}, null);

        List<IdentityKey> initialEntitySet = Arrays.asList(
                new ConstantIdentityKey("GOOG"),
                new ConstantIdentityKey("AAPL")
        );
        MessageChannel<InstrumentMessage> publisher = db.getTopicDB().createPublisher(topicKey, new PublisherPreferences().setInitialEntitySet(initialEntitySet), new YieldingIdleStrategy());
        publisher.close();
    }


}
