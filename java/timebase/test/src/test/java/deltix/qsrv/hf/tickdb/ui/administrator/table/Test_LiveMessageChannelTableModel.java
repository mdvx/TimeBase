package deltix.qsrv.hf.tickdb.ui.administrator.table;

import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.StreamConfigurationHelper;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.TimebaseTestUtils.DataGeneratorEx;
import deltix.qsrv.hf.tickdb.ui.administrator.TBARunner;

import java.util.*;

public class Test_LiveMessageChannelTableModel extends TBARunner {

    @Override
    public void test() {
        RecordClassDescriptor marketMsgDescriptor =
                StreamConfigurationHelper.mkMarketMessageDescriptor(840);

        RecordClassDescriptor bbo = StreamConfigurationHelper.mkBBOMessageDescriptor(marketMsgDescriptor,
                true, "", 840, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO);

        RecordClassDescriptor trade = StreamConfigurationHelper.mkTradeMessageDescriptor(marketMsgDescriptor,
                "", 840, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO);

        RecordClassDescriptor l2 = StreamConfigurationHelper.mkLevel2MessageDescriptor(marketMsgDescriptor,
                "", 840, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO);

        RecordClassDescriptor l2snapshot = StreamConfigurationHelper.mkL2SnapshotMessageDescriptor(marketMsgDescriptor,
                "", 840, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO);

        DXTickStream existStream = runner.getTickDb().getStream(STREAM_NAME);
        if (existStream != null)
            existStream.delete();
        DXTickStream stream = runner.getTickDb().createStream(STREAM_NAME,
                StreamOptions.polymorphic(StreamScope.DURABLE,
                        STREAM_NAME,
                        null,
                        DISTRIBUTION_FACTOR,
                        bbo, trade, l2, l2snapshot));
        StringBuffer ch = new StringBuffer("AAAAA");
        String[] symbols = new String[SYMBOLS_COUNT];
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = ch.toString();
            increment(ch, 4);
        }

        DataGeneratorEx generator = new DataGeneratorEx(new GregorianCalendar(2000, 1, 1), 1, symbols);
        LoadingOptions options = new LoadingOptions();
        TickLoader loader = stream.createLoader(options);

        long t0 = System.currentTimeMillis();
        long count = 0;

        while (generator.next() && count < TOTAL) {
            loader.send(generator.getMessage());
            count++;
            if (count % (TOTAL / MESSAGE_INFO_FREQUENCY) == 0)
                System.out.printf("Send  %,3d messages\n", count);
        }

        long t1 = System.currentTimeMillis();
        double s = (t1 - t0) * 0.001;
        System.out.printf(
                "%,d messages in %,.3fs; speed: %,.0f msg/s\n",
                count,
                s,
                count / s
        );

        loader.close();

        long t2 = System.currentTimeMillis();
        System.out.printf("Flushing time = %,.3fs\n", (t2 - t1) * 0.001);

        s = (t2 - t0) * 0.001;
        System.out.printf(
                "Overall: %,d messages in %,.3fs; speed: %,.0f msg/s\n",
                count,
                s,
                count / s
        );
    }

    public static void main(String[] args) throws Throwable {
        Test_LiveMessageChannelTableModel test = new Test_LiveMessageChannelTableModel();
        test.start();
        test.test();
        test.stop();
    }
}
