package deltix.qsrv.hf.tickdb;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.TickCursor;
import deltix.qsrv.hf.tickdb.server.ServerRunner;
import deltix.qsrv.hf.tickdb.testframework.TestAllTypesStreamCreator;
import deltix.qsrv.util.json.DataEncoding;
import deltix.qsrv.util.json.JSONHelper;
import deltix.qsrv.util.json.JSONRawMessagePrinter;
import deltix.qsrv.util.json.PrintType;
import deltix.util.JUnitCategories.TickDBFast;
import deltix.util.collections.generated.ObjectArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * @author Daniil Yarmalkevich
 * Date: 11/6/2019
 */
@Category(TickDBFast.class)
public class Test_JsonSender {

    private static TDBRunner runner;

    @BeforeClass
    public static void start() throws Throwable {
        runner = new ServerRunner(true, false);
        runner.startup();
    }

    @AfterClass
    public static void stop() throws Throwable {
        runner.shutdown();
        runner = null;
    }

    @Test
    public void testAllTypesStream() {
        DXTickDB db = runner.getServerDb();

        TestAllTypesStreamCreator creator = new TestAllTypesStreamCreator(db);
        creator.createStream();

        DXTickStream stream = db.getStream(TestAllTypesStreamCreator.STREAM_KEY);

        creator.loadTestData(ChannelQualityOfService.MAX_THROUGHPUT);

        ObjectArrayList<InstrumentMessage> nativeMessages = new ObjectArrayList<>();

        try (TickCursor cursor = db.select(Long.MIN_VALUE, new SelectionOptions(false, false), stream)) {
            while (cursor.next()) {
                nativeMessages.add(cursor.getMessage().clone());
            }
        }

        SelectionOptions options = new SelectionOptions(true, false);
        JSONRawMessagePrinter printer = new JSONRawMessagePrinter(false, true, DataEncoding.STANDARD,
                false, true, PrintType.FULL);
        StringBuilder arrayBuilder = new StringBuilder();
        arrayBuilder.append('[');
        HashSet<String> messages = new HashSet<>();
        ObjectArrayList<String> messagesList = new ObjectArrayList<>();
        ObjectArrayList<String> jsonMessagesList = new ObjectArrayList<>();
        StringBuilder temp = new StringBuilder();
        try (TickCursor cursor = db.select(Long.MIN_VALUE, options, stream)) {
            while (cursor.next()) {
                RawMessage raw = (RawMessage) cursor.getMessage();
                messages.add(raw.toString());
                messagesList.add(raw.toString());
                printer.append(raw, arrayBuilder);
                printer.append(raw, temp);
                jsonMessagesList.add(temp.toString());
                temp.setLength(0);
                arrayBuilder.append(",");
            }
        } finally {
            arrayBuilder.setLength(arrayBuilder.length() - 1);
            arrayBuilder.append(']');
        }
        String array = arrayBuilder.toString();
//        System.out.println(array);
        JsonArray jsonArray = new JsonParser().parse(array).getAsJsonArray();
        assertEquals(messages.size(), jsonArray.size());

        stream.clear();

        JSONHelper.parseAndLoad(array, stream);

        int count = 0;
        try (TickCursor cursor = db.select(Long.MIN_VALUE, new SelectionOptions(false, false), stream)) {
            while (cursor.next()) {
                assertEquals(nativeMessages.get(count), cursor.getMessage().clone());
                count++;
            }
        }

//        System.out.println(messages.size());
        count = 0;
        try (TickCursor cursor = db.select(Long.MIN_VALUE, options, stream)) {
            while (cursor.next()) {
                RawMessage raw = (RawMessage) cursor.getMessage();
//                System.out.printf("%d, ", count);
                printer.append(raw, temp);
//                assertTrue(String.format("%d %d %s", count, messages.size(), raw.toString()), messages.contains(raw.toString()));
                assertEquals(String.format("%d, \n%s, \n%s, \n%s\n", count, raw.toString(), messagesList.get(count), jsonMessagesList.get(count)),
                        raw.toString(),
                        messagesList.get(count));
                assertEquals(String.format("%d, \n%s, \n%s, \n%s\n", count, raw.toString(), messagesList.get(count), jsonMessagesList.get(count)),
                        temp.toString(),
                        jsonMessagesList.get(count));
                count++;
                temp.setLength(0);
            }
        }
    }
}
