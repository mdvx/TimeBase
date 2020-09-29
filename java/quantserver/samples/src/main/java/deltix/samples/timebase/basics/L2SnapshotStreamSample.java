package deltix.samples.timebase.basics;

import deltix.timebase.api.messages.IdentityKey;
import deltix.timebase.api.messages.InstrumentMessage;

import deltix.qsrv.hf.pub.md.FloatDataType;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.timebase.api.messages.L2SnapshotHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.text.ParseException;
import java.util.Random;

public class L2SnapshotStreamSample {
    public static final String              STREAM_KEY = "l2snapshot.stream";

    /**
     *  Instead of programmatic creation illustrated below, the user will
     *  more likely create the stream using TimeBase Administrator.
     */
    public static void      createSampleStream (DXTickDB db) {
        DXTickStream            stream = db.getStream (STREAM_KEY);

        if (stream == null) {
            stream =
                db.createStream (
                    STREAM_KEY,
                    STREAM_KEY,
                    "Description Line1\nLine 2\nLine 3",
                    0
                );

            stream.setPolymorphic(
                    StreamConfigurationHelper.mkL2SnapshotMessageDescriptor(
                            null, "NYMEX", 840, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO));
            
        }
    }

    @SuppressFBWarnings(value = "PREDICTABLE_RANDOM", justification = "This is a sample code, Random here is not used for cryptography")
    public static void      loadData (DXTickDB db) throws ParseException {
        DXTickStream            stream = db.getStream (STREAM_KEY);
        //
        //  Create reusable message objects.
        //
        final deltix.timebase.api.messages.L2SnapshotMessage snapshotMessage = new deltix.timebase.api.messages.L2SnapshotMessage();
        //
        //  Initialize fields that will not change inside the loop.
        //
        snapshot

        snapshotMessage.setSymbol("IBM");

        snapshotMessage.setMaxMarketDepth((short)5); // up to 10
        
        final TickLoader        loader = stream.createLoader ();
        final long              time = System.currentTimeMillis ();
        final double            simulatedTick = 0.05;
        final Random            rnd = new Random();
        //
        //  In this example, generate 10 "snapshots" per second 
        //  for 10 minutes, beginning with current system time.
        //
        for (int i = 0; i < 6000; i++) {
            final double        simulatedMidPrice = simulatedTick * 20.0 + (i % 20);
            final double        simulatedTopSize = ((i % 10) + 1) * 100.0;
            final long          simulatedTimestamp = time + i * 100; // Every 100ms
                     
            snapshotMessage.setTimeStampMs(simulatedTimestamp);
            
            snapshotMessage.setIsAsk(false); // bid side first
            final int           bidLevels = rnd.nextInt(snapshotMessage.getMaxMarketDepth()) + 1;
            for (int ii = 0; ii < bidLevels; ii++) {
                L2SnapshotHelper.setPrice(snapshotMessage, ii, simulatedMidPrice - (simulatedTick * (ii + 1)));
                L2SnapshotHelper.setSize(snapshotMessage, ii,  simulatedTopSize + (simulatedTopSize / snapshotMessage.getMaxMarketDepth()) * ii);
            }
            L2SnapshotHelper.clearTail(snapshotMessage, bidLevels);

            loader.send(snapshotMessage);

            snapshotMessage.setIsAsk(true); // and then ask side
            final int           askLevels = rnd.nextInt(snapshotMessage.getMaxMarketDepth()) + 1;
            for (int ii = 0; ii < askLevels; ii++) {
                L2SnapshotHelper.setPrice(snapshotMessage, ii, simulatedMidPrice + (simulatedTick * (ii + 1)));
                L2SnapshotHelper.setSize(snapshotMessage, ii,  simulatedTopSize + (simulatedTopSize / snapshotMessage.getMaxMarketDepth()) * ii);
            }
            L2SnapshotHelper.clearTail(snapshotMessage, askLevels);

            loader.send(snapshotMessage);
        }

        loader.close ();

        System.out.println ("Done.");
    }

    public static void      readData (DXTickDB db) {
        DXTickStream            stream = db.getStream (STREAM_KEY);
                
        //
        //  List of entities to subscribe (if null, all stream entities will be used)
        //
        IdentityKey[] entities = null;

        //
        //  List of types to subscribe - select trades and bbo's only
        //
        String[] types = new String[] {
                deltix.timebase.api.messages.L2SnapshotMessage.class.getName()
        };

        //
        //  Additional options for select()
        //
        SelectionOptions options = new SelectionOptions();
        options.raw = false; // use decoding
        
        //
        //  Cursor is equivalent to a JDBC ResultSet
        //
        TickCursor              cursor = stream.select (Long.MIN_VALUE, options, types, entities);

        try {
            while (cursor.next ()) {
                final InstrumentMessage msg = cursor.getMessage ();
                
                if (msg instanceof deltix.timebase.api.messages.L2SnapshotMessage) {
                    final deltix.timebase.api.messages.L2SnapshotMessage snapshot = (deltix.timebase.api.messages.L2SnapshotMessage) msg;

                    System.out.println (snapshot);
                }
                else
                    throw new RuntimeException ("Unrecognized message type: " + msg);
            }
        } finally {
            cursor.close ();
        }

        System.out.println ("Done.");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            args = new String [] { "dxtick://localhost:8011" };

        DXTickDB    db = TickDBFactory.createFromUrl (args [0]);

        db.open (false);

        try {
            createSampleStream (db);
            loadData (db);            
            readData (db);
        } finally {
            db.close ();
        }
    }
}

