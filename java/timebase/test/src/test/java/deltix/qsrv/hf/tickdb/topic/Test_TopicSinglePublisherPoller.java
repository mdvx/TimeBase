package deltix.qsrv.hf.tickdb.topic;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.util.net.NetworkInterfaceUtil;
import deltix.qsrv.hf.tickdb.pub.topic.TopicDB;
import deltix.qsrv.hf.tickdb.pub.topic.settings.MulticastTopicSettings;
import deltix.qsrv.hf.tickdb.pub.topic.settings.TopicSettings;
import deltix.util.JUnitCategories;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexei Osipov
 */
@Category(JUnitCategories.TickDB.class)
public class Test_TopicSinglePublisherPoller extends Test_TopicPollerBase {

    @Ignore("TODO: Find Race condition")
    public void test() throws Exception {
        executeTest();
    }

    @Override
    protected void createTopic(TopicDB topicDB, String topicKey, RecordClassDescriptor[] types) {
        MulticastTopicSettings settings = new MulticastTopicSettings();
        settings.setTtl(1);
        topicDB.createTopic(topicKey, types,  new TopicSettings().setSinglePublisherUdpMode(NetworkInterfaceUtil.getOwnPublicAddressAsText()));
    }
}