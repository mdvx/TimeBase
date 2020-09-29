package deltix.qsrv.hf.tickdb.topic;

import deltix.util.JUnitCategories;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexei Osipov
 */
@Category(JUnitCategories.TickDB.class)
public class Test_TopicPoller extends Test_TopicPollerBase {
    public Test_TopicPoller() {
    }

    @Test(timeout = TEST_TIMEOUT)
    public void test() throws Exception {
        executeTest();
    }
}