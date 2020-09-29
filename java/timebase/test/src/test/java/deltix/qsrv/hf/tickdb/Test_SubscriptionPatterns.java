package deltix.qsrv.hf.tickdb;

import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.IdentityKey;

import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.testsetup.TickDBCreator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Test_SubscriptionPatterns extends TDBRunnerBase {

    private DXTickStream getBars () {
        DXTickDB db = getTickDb();

        DXTickStream tickStream = db.getStream(TickDBCreator.BARS_STREAM_KEY);
        if (tickStream == null)
            tickStream = TickDBCreator.createBarsStream (db, TickDBCreator.BARS_STREAM_KEY);

        return tickStream;
    }

    @Test
    public void test() {
        try (TickCursor cursor = getBars().select(0, new SelectionOptions(), null,
                new IdentityKey[] { new ConstantIdentityKey("*") }))
        {
            assertEquals (94851, countMessages (cursor));
        }
    }

    private int             countMessages (TickCursor cursor) {
        int                 count = 0;

        while (cursor.next ())
            count++;

        return (count);
    }

}
