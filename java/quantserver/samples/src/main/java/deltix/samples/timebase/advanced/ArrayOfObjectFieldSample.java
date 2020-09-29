package deltix.samples.timebase.advanced;

import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.timebase.api.messages.IdentityKey;

import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedUnboundEncoder;
import deltix.qsrv.hf.pub.codec.NonStaticFieldInfo;
import deltix.qsrv.hf.pub.codec.UnboundDecoder;
import deltix.qsrv.hf.pub.codec.UnboundEncoder;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.pub.md.Introspector.IntrospectionException;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.qsrv.hf.tickdb.pub.LoadingOptions;
import deltix.qsrv.hf.tickdb.pub.SelectionOptions;
import deltix.qsrv.hf.tickdb.pub.TickCursor;
import deltix.qsrv.hf.tickdb.pub.TickDBFactory;
import deltix.qsrv.hf.tickdb.pub.TickLoader;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;

/**
 * Demonstrates how to create, write and read a stream with a field of an array type (element of OBJECT type)
 */
public class ArrayOfObjectFieldSample {
    public static final String STREAM_KEY = "arr.obj.stream";

    private static final RecordClassDescriptor MARKET_SNAPSHOT_CLASS;
    private static final RecordClassDescriptor SNAPSHOT_ENTRY_CLASS;

    static {
        try {
            MARKET_SNAPSHOT_CLASS = (RecordClassDescriptor) Introspector.introspectSingleClass(MDL2SnapshotMessage.class);
            ArrayDataType type = (ArrayDataType) MARKET_SNAPSHOT_CLASS.getField("entries").getType();

            SNAPSHOT_ENTRY_CLASS = ((ClassDataType)type.getElementDataType()).getFixedDescriptor();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createSampleStream(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);

        if (stream != null)
            stream.delete();

        stream = db.createStream(
                        STREAM_KEY,
                        STREAM_KEY,
                        "Description Line1\nLine 2\nLine 3",
                        0
                );

        stream.setFixedType(MARKET_SNAPSHOT_CLASS);

    }

    public static void readDataBound(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);

        //
        //  List of entities to subscribe (if null, all stream entities will be used)
        //
        IdentityKey[] entities = null;

        //
        //  List of types to subscribe - select only "MyClass" messages
        //
        String[] types = new String[]{ MARKET_SNAPSHOT_CLASS.getName() };

        //
        //  Cursor is equivalent to a JDBC ResultSet
        //
        try (TickCursor cursor = stream.select(Long.MIN_VALUE, null, types, entities)){
            while (cursor.next()) {
                //
                //  We can safely cast to MDL2SnapshotMessage because we have requested only this type
                //
                MDL2SnapshotMessage msg = (MDL2SnapshotMessage) cursor.getMessage();
                //
                //  Print out standard fields
                //
                System.out.printf(
                        "%tT.%<tL %s %s %s",
                        msg.getTimeStampMs(),
                        msg.getSymbol(),
                        msg.getInstrumentType().name(),
                        toString(msg)
                );
                // TODO: output entries

                System.out.println();
            }
        }
    }

    public static void readDataUnbound(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);
        RecordClassDescriptor classDescriptor = stream.getFixedType();
        MemoryDataInput in = new MemoryDataInput ();
        UnboundDecoder decoder =
                CodecFactory.COMPILED.createFixedUnboundDecoder(classDescriptor);

        //
        //  Always use raw = true for custom messages.
        //
        SelectionOptions options = new SelectionOptions (true, false);

        //
        //  List of entities to subscribe (if null, all stream entities will be used)
        //
        IdentityKey[] entities = null;

        //
        //  List of types to subscribe - select only "MDL2SnapshotMessage" messages
        //
        String[] types = new String[]{classDescriptor.getName()};

        //
        //  Cursor is equivalent to a JDBC ResultSet
        //
        try (TickCursor cursor = stream.select(Long.MIN_VALUE, options, types, entities)){
            while (cursor.next()) {
                //
                //  We can safely cast to RawMessage because we have requested
                //  a raw message cursor.
                //
                RawMessage msg = (RawMessage) cursor.getMessage();
                //
                //  Print out standard fields
                //
                System.out.printf (
                        "%tT.%<tL %s %s",
                        msg.getTimeStampMs(),
                        msg.getSymbol(),
                        msg.getInstrumentType().name ()
                );
                //
                //  Iterate over custom fields.
                //
                in.setBytes (msg.data, msg.offset, msg.length);
                decoder.beginRead (in);

                decoder.nextField (); // requestId
                NonStaticFieldInfo df = decoder.getField();
                System.out.printf(",%s: %s", df.getName(), decoder.getString());

                decoder.nextField (); // sequenceId

                decoder.nextField(); // entries
                //
                //  In case of array type getString () method has a little usefulness.
                //
                df = decoder.getField();
                DataType type = df.getType();
                System.out.printf(",%s: %s", df.getName(), UnboundUtils.readArray((ArrayDataType) type, decoder));

                System.out.println ();
            }
        }
    }

//    public static class TestMessage extends MDL2SnapshotMessage {
//        public int sequence = -1;
//    }

    public static void writeIntoStreamBound(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);

        MDL2SnapshotMessage msg = new MDL2SnapshotMessage();
        //
        //  Set up standard fields
        //
        msg.setSymbol("AAPL");

        msg.sequenceId = IntegerDataType.INT64_NULL;

        // and two 1st level entries
        MDEntry offer1 = getEntry();
        offer1.entryType = MDEntryType.Offer;
        offer1.originator = "PROVIDER1";
        msg.addEntry(offer1);

        MDEntry offer2 = getEntry();
        offer2.entryType = MDEntryType.Offer;
        offer2.originator = "PROVIDER2";
        msg.addEntry(offer2);

        MDEntry bid1 = getEntry();
        bid1.entryType = MDEntryType.Bid;
        bid1.originator = "PROVIDER1";
        msg.addEntry(bid1);

        MDEntry bid2 = getEntry();
        bid2.entryType = MDEntryType.Bid;
        bid2.originator = "PROVIDER2";
        msg.addEntry(bid2);

        LoadingOptions options = new LoadingOptions();
        options.channelQOS = ChannelQualityOfService.MIN_INIT_TIME;

        try (TickLoader loader = stream.createLoader(options)) {
            //  Generate a few messages
            for (int ii = 1; ii < 10; ii++) {
                msg.requestId= "req_" + ii;

                bid1.price = ii - 0.5;
                bid2.price = ii - 0.7;

                offer1.price = ii + 0.7;
                offer2.price = ii + 0.5;

                loader.send(msg);
            }
        }
    }

    public static void writeIntoStreamUnbound(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);
        RecordClassDescriptor classDescriptor = stream.getFixedType();
        MemoryDataOutput out = new MemoryDataOutput();
        FixedUnboundEncoder encoder =
                CodecFactory.COMPILED.createFixedUnboundEncoder(classDescriptor);

        RawMessage msg = new RawMessage();


        //
        //  Set up standard fields
        //
        msg.setSymbol("AAPL");

        msg.type = classDescriptor;

        try (TickLoader loader = stream.createLoader(new LoadingOptions(true))) {
            //  Generate a few messages
            for (int ii = 1; ii < 10; ii++) {
                out.reset();
                encoder.beginWrite(out);
                encoder.nextField(); // requestId
                encoder.writeString("req_" + ii);

                encoder.nextField(); // sequenceId

                encoder.nextField(); // entries
                encoder.setArrayLength(2);

                UnboundEncoder uenc = encoder.nextWritableElement().getFieldEncoder(SNAPSHOT_ENTRY_CLASS);
                uenc.nextField(); // size
                uenc.writeDouble(100);
                uenc.nextField(); // price
                uenc.writeDouble(ii + 0.5);
                uenc.nextField(); // currencyCode
                uenc.writeInt(999);
                uenc.nextField(); // quoteId
                uenc.writeString("132354546_B");
                uenc.nextField(); // originator
                uenc.writeString("PROVIDER");
                uenc.nextField(); // quoteCondition
                uenc.writeLong(MDQuoteCondition.Active.ordinal());
                uenc.nextField(); // positionNo
                uenc.writeInt(0);
                uenc.nextField(); // entryType
                uenc.writeLong(MDEntryType.Offer.ordinal());

                uenc = encoder.nextWritableElement().getFieldEncoder(SNAPSHOT_ENTRY_CLASS);
                uenc.nextField(); // size
                uenc.writeDouble(100);
                uenc.nextField(); // price
                uenc.writeDouble(ii - 0.5);
                uenc.nextField(); // currencyCode
                uenc.writeInt(999);
                uenc.nextField(); // quoteId
                uenc.writeString("132354546_B");
                uenc.nextField(); // originator
                uenc.writeString("PROVIDER");
                uenc.nextField(); // quoteCondition
                uenc.writeLong(MDQuoteCondition.Active.ordinal());
                uenc.nextField(); // positionNo
                uenc.writeInt(0);
                uenc.nextField(); // entryType
                uenc.writeLong(MDEntryType.Bid.ordinal());
                encoder.endWrite();

                msg.setBytes(out);
                loader.send(msg);
            }
        }
    }

    private static MDEntry getEntry() {
        final MDEntry e = new MDEntry();
        e.size = 100;
        e.currencyCode = 999;
        e.quoteId = "132354546_B";
        //e.originator = "PROVIDER";
        e.quoteCondition = MDQuoteCondition.Active;
        e.positionNo = 0;
        return e;
    }


    private static String toString(MDL2SnapshotMessage snapshot) {
        final StringBuilder sb = new StringBuilder();
        sb.append(snapshot.requestId).append(',');

        if (snapshot.entries == null)
            sb.append("null");
        else {
            sb.append('[');
            for (MDEntry entry : snapshot.entries) {
                if (entry != null)
                    sb.append(toString(entry)).append(',');
                else
                    sb.append("null,");
            }
            sb.append(']');
        }
        return sb.toString();
    }

    private static String toString(MDEntry entry) {
        return String.format(
                "(%.2f,%.2f,%d,%s,%s,%s,%d,%s)",
                entry.price,
                entry.size,
                entry.currencyCode,
                entry.quoteId,
                entry.originator,
                entry.quoteCondition,
                entry.positionNo,
                entry.entryType);
    }

    public static void main(String[] args) {
        if (args.length == 0)
            args = new String[]{"dxtick://localhost:8011"};

        DXTickDB db = TickDBFactory.createFromUrl(args[0]);

        db.open(false);

        try {
            createSampleStream(db);
            writeIntoStreamBound(db);
            writeIntoStreamUnbound(db);
            readDataBound(db);
            readDataUnbound(db);
        } finally {
            db.close();
        }
    }
}
