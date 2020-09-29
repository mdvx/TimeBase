package deltix.samples.timebase.advanced;

import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedUnboundEncoder;
import deltix.qsrv.hf.pub.codec.UnboundEncoder;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.pub.*;

import deltix.timebase.api.messages.BookUpdateAction;
import deltix.util.memory.MemoryDataOutput;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static java.lang.Math.*;

import java.text.DecimalFormat;
import java.util.*;

public class L2NewStreamSample {
    public  static final String                STREAM_KEY             = "l2new.stream";
    public  static final String                TIMEBASE_URL            = "dxtick://localhost:8011";

    public  static final int                   LEVEL                  = 20;

    private static       RecordClassDescriptor MARKET_CLASS           =
            StreamConfigurationHelper.mkMarketMessageDescriptor(840);

    private static final RecordClassDescriptor LEVEL2_ACTION_CLASS    =
            StreamConfigurationHelper.mkLevel2ActionDescriptor          (FloatDataType.ENCODING_SCALE_AUTO,
                                                                         FloatDataType.ENCODING_SCALE_AUTO );
    private static final RecordClassDescriptor LEVEL2_MSF_CLASS =
            StreamConfigurationHelper.mkLevel2IncrementMessageDescriptor(MARKET_CLASS,
                                                                         "",
                                                                         840,
                                                                         LEVEL2_ACTION_CLASS               );


    public static DXTickStream createSampleStream(DXTickDB db) {
        DXTickStream stream = db.getStream(STREAM_KEY);

        if (stream != null) {
            stream.delete();
        }

        return
                db.createStream(
                        STREAM_KEY,
                        StreamOptions.polymorphic(StreamScope.DURABLE,
                                STREAM_KEY,
                                "L2 new format stream",
                                0,
                                LEVEL2_ACTION_CLASS, LEVEL2_MSF_CLASS)

                );

    }

    @SuppressFBWarnings(value = "PREDICTABLE_RANDOM", justification = "This is a sample code, Random here is not used for cryptography")
    public static void writeIntoStream(DXTickStream stream) {

        //  Always use raw = true for custom messages.
        LoadingOptions options = new LoadingOptions(true);
        TickLoader loader = stream.createLoader(options);

        //  Re-usable buffer for collecting the encoded message
        MemoryDataOutput dataOutput = new MemoryDataOutput();
        //FixedUnboundEncoder shapShotEncoder =
        //        CodecFactory.COMPILED.createFixedUnboundEncoder(LEVEL2_SNAPSHOT_CLASS);

        FixedUnboundEncoder incrementEncoder =
                CodecFactory.COMPILED.createFixedUnboundEncoder(LEVEL2_MSF_CLASS);

        RawMessage rawMessage = new RawMessage();
        Random r = new Random(System.currentTimeMillis());

        try {
            //  Generate a few messages
            for (int ii = 1; ii < 1000; ii++) {
                //
                //  Set up standard fields
                //
                rawMessage.setTimeStampMs(System.currentTimeMillis());
                rawMessage.setSymbol("AAPL");
                raw
                
                rawMessage.type = LEVEL2_MSF_CLASS;
                //
                //  Set up custom fields
                //
                dataOutput.reset();
                incrementEncoder.beginWrite(dataOutput);
                //
                //  Fields must be set in the order the encoder
                //  expects them, which in the case of a fixed-type stream
                //  with a non-inherited class descriptor is equivalent to the
                //  order of the class descriptor's fields.
                //
                incrementEncoder.nextField();//sequenceNumber
                incrementEncoder.writeInt(ii);

                incrementEncoder.nextField(); // entries
                int level = r.nextInt(LEVEL) + 10;
                incrementEncoder.setArrayLength(level);
                writeObject(incrementEncoder, level, ii);  // actions

                writeFieldValues(incrementEncoder, ii % 10 == 0, ii);  //isImplied, isSnapshot, sequenceId


                if (incrementEncoder.nextField()) // make sure we are at end
                {
                    throw new RuntimeException("unexpected field: " + incrementEncoder.getField().toString());
                }

                incrementEncoder.endWrite();

                rawMessage.setBytes(dataOutput, 0);
                loader.send(rawMessage);
            }
        } finally {
            loader.close();
        }
    }

    private static void writeObject(WritableValue wv, int level, int sequenceNumber) {

        UnboundEncoder uenc;
        DecimalFormat df = new DecimalFormat(".##");

        for (int i = 0; i < level; i++) {
            uenc = wv.nextWritableElement().getFieldEncoder(LEVEL2_ACTION_CLASS);

            uenc.nextField();
            uenc.writeInt(i);  //level

            uenc.nextField();
            uenc.writeBoolean(i % 2 == 0);//isAsk

            uenc.nextField();
            uenc.writeLong(
                    i % 2 == 0 ? BookUpdateAction.UPDATE.ordinal() : BookUpdateAction.INSERT.ordinal());  //book action

            uenc.nextField();
            uenc.writeDouble(Double.valueOf(df.format(585 + abs(sin(i + level)))));   //price

            uenc.nextField();
            uenc.writeDouble(Double.valueOf(df.format(2000 * abs(sin(i + level)))));   //size


            uenc.nextField();
            uenc.writeInt(i);   //numOfOrders

            uenc.endWrite();
        }
    }


    private static void writeFieldValues(final FixedUnboundEncoder encoder, boolean isSnapshot, long sequenceId){
        encoder.nextField(); // isImplied
        encoder.writeBoolean(false);

        encoder.nextField(); // isSnapshot
        encoder.writeBoolean(isSnapshot);        
        
        encoder.nextField(); // sequenceId
        encoder.writeLong(sequenceId);
    }

    public static void      main (String [] args) {
        if (args.length == 0)
            args = new String [] { TIMEBASE_URL };

        DXTickDB db = TickDBFactory.createFromUrl(args[0]);

        db.open (false);

        try {
            DXTickStream stream = createSampleStream (db);
            writeIntoStream (stream);
        } finally {
            db.close ();
        }
    }

}
