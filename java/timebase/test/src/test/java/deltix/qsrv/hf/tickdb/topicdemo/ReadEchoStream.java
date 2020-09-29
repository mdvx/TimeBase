package deltix.qsrv.hf.tickdb.topicdemo;

import deltix.qsrv.hf.pub.ChannelPerformance;
import deltix.qsrv.hf.tickdb.pub.RemoteTickDB;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.TickCursor;
import deltix.qsrv.hf.tickdb.pub.topic.MessageProcessor;
import deltix.qsrv.hf.tickdb.topicdemo.util.DemoConf;
import deltix.qsrv.hf.tickdb.topicdemo.util.ExperimentFormat;
import deltix.util.concurrent.CursorIsClosedException;

import java.util.function.BooleanSupplier;

/**
 * @author Alexei Osipov
 */
public class ReadEchoStream extends ReadEchoBase {
    private final ChannelPerformance channelPerformance;

    private TickCursor cursor = null;

    public ReadEchoStream(ChannelPerformance channelPerformance, ExperimentFormat experimentFormat) {
        super(experimentFormat);
        this.channelPerformance = channelPerformance;
    }

    @Override
    protected void work(RemoteTickDB client, BooleanSupplier stopCondition, MessageProcessor messageProcessor) {
        SelectionOptions options = new SelectionOptions();
        options.channelPerformance = this.channelPerformance;
        options.live = true;
        String streamKey = experimentFormat.useMainChannel() ? DemoConf.DEMO_MAIN_STREAM :  DemoConf.DEMO_ECHO_STREAM;
        this.cursor = client.getStream(streamKey).createCursor(options);
        cursor.subscribeToAllEntities();
        cursor.subscribeToAllTypes();
        cursor.reset(Long.MIN_VALUE);

        // Process messages from topic until stop signal triggered
        try {
            while (!stopCondition.getAsBoolean() && cursor.next()) {
                messageProcessor.process(cursor.getMessage());
            }
        } catch (CursorIsClosedException exception) {
            System.out.println("Echo reader cursor was closed");
        } finally {
            cursor.close();
        }
    }

    @Override
    public void stop() {
        // This is necessary because reader may block indefinitely
        if (cursor != null) {
            cursor.close();
        }
    }
}
