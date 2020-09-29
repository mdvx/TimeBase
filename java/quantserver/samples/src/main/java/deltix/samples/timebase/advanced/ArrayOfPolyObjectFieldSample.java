package deltix.samples.timebase.advanced;

import deltix.timebase.api.messages.IdentityKey;

import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.SimpleTypeLoader;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedUnboundEncoder;
import deltix.qsrv.hf.pub.codec.NonStaticFieldInfo;
import deltix.qsrv.hf.pub.codec.UnboundDecoder;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.pub.md.Introspector.IntrospectionException;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;
import deltix.util.collections.generated.ObjectArrayList;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;

/**
 * Demonstrates how to create, write and read a stream with a field of an array type (element of OBJECT type)
 */
public class ArrayOfPolyObjectFieldSample {
    public static final String STREAM_KEY = "arr.poly.obj.stream";

    private static final RecordClassDescriptor POLY_ARRAY_CLASS;

    static {
        try {
            POLY_ARRAY_CLASS = (RecordClassDescriptor) Introspector.introspectSingleClass(MsgPolyArray.class);
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

        stream.setFixedType(POLY_ARRAY_CLASS);

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
        String[] types = new String[]{POLY_ARRAY_CLASS.getName()};

        SelectionOptions            options = new SelectionOptions ();

        options.typeLoader = new SimpleTypeLoader(null, MsgPolyArray.class);

        //
        //  Cursor is equivalent to a JDBC ResultSet
        //
        //try (TickCursor cursor = stream.select(Long.MIN_VALUE, null, types, entities)){
        try (InstrumentMessageSource cursor = db.executeQuery("select * from \"" + STREAM_KEY + "\"", options)) {
            while (cursor.next()) {
                //
                //  We can safely cast to MsgPolyArray because we have requested only this type
                //
                MsgPolyArray msg = (MsgPolyArray) cursor.getMessage();
                //
                //  Print out standard fields
                //
                System.out.printf(
                        "%tT.%<tL %s %s %s",
                        msg.getTimeStampMs(),
                        msg.getSymbol(),
                        msg.getInstrumentType().name(),
                        msg.toString()
                );

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
        try (TickCursor cursor = stream.select(Long.MIN_VALUE, options, types, entities)) {

        //try (InstrumentMessageSource cursor = db.executeQuery("select * from \"" + STREAM_KEY + "\"")){
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

                decoder.nextField(); // poly
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

    private static final int NUM_OF_ELEMENTS = 5;
    // This data doesn't concern API, but facilitates its demonstration
    private static final Object UNBOUND_ORDER_DATA[][] = {
            {0},
            {1, 500.05, 0.05},
            {2, 510.05},
            {1, 503.05, 3.05},
            {1, 501.05, 8.05}
    };

    private static final OrderType BOUND_ORDER_DATA[] = {
            new MarketOrderType(),
            new LimitOrder((Double)UNBOUND_ORDER_DATA[1][1], (Double)UNBOUND_ORDER_DATA[1][2], "0"),
            new StopOrder((Double)UNBOUND_ORDER_DATA[2][1]),
            new LimitOrder((Double)UNBOUND_ORDER_DATA[3][1], (Double)UNBOUND_ORDER_DATA[3][2], "1"),
            new LimitOrder((Double)UNBOUND_ORDER_DATA[4][1], (Double)UNBOUND_ORDER_DATA[4][2], "2")
    };

    public static void writeIntoStreamBound(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);

        MsgPolyArray msg = new MsgPolyArray();
        //
        //  Set up standard fields
        //
        msg.setSymbol("AAPL");

        msg.poly = new ObjectArrayList<>();

        try (TickLoader loader = stream.createLoader()) {
            //  Generate a few messages
            for (int ii = 1; ii < 5; ii++) {
                msg.requestId = "req_" + ii;
                msg.poly.clear();
                for (int i = 0; i < NUM_OF_ELEMENTS; i++) {
                    msg.poly.add(BOUND_ORDER_DATA[i]);
                }

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

        // substitute 1st element with RCD from Timebase
        final RecordClassDescriptor[] rcds = ((ClassDataType) ((ArrayDataType) classDescriptor.getField("poly").getType()).getElementDataType()).getDescriptors();
        for (int i = 0; i < NUM_OF_ELEMENTS; i++) {
            UNBOUND_ORDER_DATA[i][0] = rcds[(Integer) UNBOUND_ORDER_DATA[i][0]];
        }

        try (TickLoader loader = stream.createLoader(new LoadingOptions(true))) {
            //  Generate a few messages
            for (int ii = 1; ii < 2; ii++) {
                out.reset();
                encoder.beginWrite(out);
                encoder.nextField(); // requestId
                encoder.writeString("req_" + ii);

                encoder.nextField(); // poly
                encoder.setArrayLength(NUM_OF_ELEMENTS);
                for (int i = 0; i < NUM_OF_ELEMENTS; i++) {
                    UnboundUtils.writeObjectPoly(UNBOUND_ORDER_DATA[i], encoder.nextWritableElement());
                }
                encoder.endWrite();

                msg.setBytes(out);
                loader.send(msg);
            }
        }
    }

    public static void main(String[] args) {

        if (args.length == 0)
            args = new String[]{"dxtick://localhost:8033"};

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
