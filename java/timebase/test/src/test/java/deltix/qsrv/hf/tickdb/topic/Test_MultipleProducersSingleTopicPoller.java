package deltix.qsrv.hf.tickdb.topic;

import deltix.util.JUnitCategories;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexei Osipov
 */
@Category(JUnitCategories.TickDB.class)
public class Test_MultipleProducersSingleTopicPoller extends Test_TopicPollerBase {
    @Test(timeout = TEST_TIMEOUT)
    public void test() throws Exception {
        executeTest(3);
    }
}