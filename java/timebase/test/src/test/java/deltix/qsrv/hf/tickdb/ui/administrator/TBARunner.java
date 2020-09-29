package deltix.qsrv.hf.tickdb.ui.administrator;

import deltix.qsrv.hf.tickdb.TDBRunner;
import deltix.qsrv.hf.tickdb.Test_LoaderPerformance;
import deltix.qsrv.hf.tickdb.pub.StreamOptions;
import org.junit.BeforeClass;

/**
 * User: TurskiyS
 * Date: 2/27/13
 */
public abstract class TBARunner extends Test_LoaderPerformance {

    protected int TOTAL = 2000000000;
    protected int SYMBOLS_COUNT = 10;
    protected int MESSAGE_INFO_FREQUENCY = 100;
    protected int DISTRIBUTION_FACTOR = 400; //StreamOptions.MAX_DISTRIBUTION;
    protected String STREAM_NAME = "test";
    static final boolean launchRemote = true;

    @BeforeClass
    public static void start() throws Throwable {
        runner = new TDBRunner(launchRemote, true);
        runner.startup();
        if (launchRemote){
            System.out.println(runner.getPort());
        }
    }

    public abstract void test();

}
