package deltix.qsrv.hf.tickdb.topicdemo;

import deltix.streaming.MessageChannel;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.topicdemo.util.DemoConf;
import deltix.util.io.idlestrat.BusySpinIdleStrategy;

/**
 * @author Alexei Osipov
 */
public class WriteTopic extends WriteBase {
    @Override
    protected MessageChannel<InstrumentMessage> createLoader(RemoteTickDB client) {
        return client.getTopicDB().createPublisher(DemoConf.DEMO_MAIN_TOPIC, null, new BusySpinIdleStrategy());
    }
}
