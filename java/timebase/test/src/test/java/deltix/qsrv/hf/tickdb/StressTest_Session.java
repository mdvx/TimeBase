package deltix.qsrv.hf.tickdb;

import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.IdentityKey;

import deltix.qsrv.hf.tickdb.server.ServerRunner;
import deltix.timebase.api.messages.TradeMessage;
import deltix.qsrv.hf.pub.md.FloatDataType;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.util.time.TimeKeeper;
import org.junit.Test;

import java.util.Random;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBStress;

@Category(TickDBStress.class)
public class StressTest_Session {

    @Test
    public void run() throws Throwable {

        TDBRunner runner = new ServerRunner(true,true);
        runner.startup();

        DXTickDB db = runner.getTickDb();

        DXTickStream stream = db.getStream("agg");
        if (stream != null)
            stream.delete();

        stream = db.createStream("agg", StreamOptions.fixedType(StreamScope.DURABLE, "agg", null, 0,
                StreamConfigurationHelper.mkTradeMessageDescriptor(null, null, null, FloatDataType.ENCODING_FIXED_DOUBLE, FloatDataType.ENCODING_FIXED_DOUBLE))
        );

        Updater[] threads = new Updater[10];

        for (int i = 0; i < threads.length; i++) {

            IdentityKey[] symbols = new IdentityKey[10];
            for (int j = 0; j < symbols.length; j++)
                symbols[j] = new ConstantIdentityKey(i + "_" + j);

            threads[i] = new Updater(i, stream, symbols);
        }


        for (int i = 0; i < threads.length; i++)
            threads[i].start();

        for (int i = 0; i < threads.length; i++)
            threads[i].join();


        runner.shutdown();
    }
}

class Updater extends Thread {

    public final DXTickStream stream;
    public final IdentityKey[] symbols;

    Updater(int id, DXTickStream stream, IdentityKey[] symbols) {
        super("Updater:" + id);

        this.stream = stream;
        this.symbols = symbols;
    }

    @Override
    public void run() {

        int count = 0;

        Random rnd = new Random(this.getId());

        TradeMessage message = new TradeMessage();

        while (count++ < 500) {

            for (IdentityKey symbol : symbols) {

                long[] range = stream.getTimeRange(symbol);

                try (TickLoader loader = stream.createLoader(new LoadingOptions(false))) {
                    try {
                        Thread.sleep(rnd.nextInt(10));
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }

                    message.setSymbol(symbol.getSymbol().toString());
                    message.setTimeStampMs(range != null ? range[1] + 1 : TimeKeeper.currentTime);

                    message.setSize(count);
                    message.setPrice(count);

                    loader.send(message);
                }
            }

            try {
                //System.out.println(this + " upload instruments done (" + count + ")");
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
