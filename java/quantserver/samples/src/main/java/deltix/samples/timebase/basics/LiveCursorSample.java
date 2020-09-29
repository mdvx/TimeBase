package deltix.samples.timebase.basics;

import deltix.timebase.api.messages.IdentityKey;
import deltix.timebase.api.messages.InstrumentMessage;

import deltix.qsrv.hf.pub.util.LiveCursorWatcher;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.timebase.api.messages.BarMessage;
import deltix.util.time.GMT;
import deltix.util.time.Interval;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class LiveCursorSample {

    public static final String              STREAM_KEY = "live.stream";

    private static final SimpleDateFormat DF = new SimpleDateFormat ("MM/dd/yyyy");
    private static final TimeZone TZ = TimeZone.getTimeZone ("GMT");

    public static DXTickStream createSampleStream (DXTickDB db) {
        DXTickStream            stream = db.getStream (STREAM_KEY);

        if (stream == null) {
            stream =
                    db.createStream (
                            STREAM_KEY,
                            STREAM_KEY,
                            "Description Line1\nLine 2\nLine 3",
                            0
                    );
            //
            // Passing stream name, exchange code, currency code, and bar size.
            //
            StreamConfigurationHelper.setBar (stream, "NYMEX", 840, Interval.SECOND);
        }

        return stream;
    }

    public static void      loadData (DXTickStream stream) throws ParseException {

        BarMessage bar = new BarMessage();

        TickLoader loader = stream.createLoader ();

        //
        // Add listener for errors that may occurs while loading data
        //
        LoadingErrorListener listener = new LoadingErrorListener() {
            @Override
            public void onError(LoadingError e) {
                System.out.println("Importing error: " + e.getMessage());
            }
        };

        loader.addEventListener(listener);

        //
        //  Always load daily bars in GMT.
        //
        DF.setTimeZone(TZ);

        Calendar calendar = Calendar.getInstance();

        for (int ii = 1; ii < 10000; ii++){
            bar.setInstrumentType(InstrumentType.EQUITY);
            bar.setSymbol("AAPL");

            //
            //  bar.timestamp sets RAW timestamp.
            //  Given this, storing data with RAW timestamp of Tuesday - Saturday 00:00:00.
            //  Following if statement skips Sunday/Monday.
            //
            if ((calendar.get(Calendar.DAY_OF_WEEK) == 1) || (calendar.get(Calendar.DAY_OF_WEEK) == 2)){
                calendar.add(Calendar.DAY_OF_WEEK, 1);
                continue;
            }

            bar.setTimeStampMs(DF.parse(DF.format(calendar.getTime())).getTime());
            bar.setOpen(ii);
            bar.setHigh(ii + .5);
            bar.setLow(ii - .5);
            bar.setClose(ii + .25);
            bar.setVolume(100000 + ii);

            loader.send (bar);

            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        loader.close();

        System.out.println ("Done.");
    }

    public static LiveCursorWatcher      monitorData (DXTickStream stream) {

        //
        //  List of entities to subscribe (if null, all stream entities will be used)
        //
        IdentityKey[] entities = null;

        //
        //  List of types to subscribe - select 'BarMessage' only
        //
        String[] types = new String[] { BarMessage.class.getName() };

        //
        //  Additional options for select()
        //
        SelectionOptions options = new SelectionOptions();
        options.raw = false; // use decoding
        options.live = true;

        //
        //  Cursor is equivalent to a JDBC ResultSet
        //
        TickCursor              cursor = stream.select (Long.MIN_VALUE, options, types, entities);

        LiveCursorWatcher       watcher = new LiveCursorWatcher(cursor, new LiveCursorWatcher.MessageListener() {
            @Override
            public void onMessage(InstrumentMessage msg) {
                BarMessage bar = (BarMessage) msg;
                System.out.println("Symbol: " + bar.getSymbol() +
                                ", Date: " + GMT.formatDate(bar.getTimeStampMs()) +
                                ", Open: " + bar.getOpen()+
                                ", High: " + bar.getHigh() +
                                ", Low: " + bar.getLow() +
                                ", Close: " + bar.getClose() +
                                ", Volume: " + bar.getVolume()
                );
            }
        });

        return watcher;
    }

    public static void main(String[] args) throws ParseException {
        if (args.length == 0)
            args = new String[]{"dxtick://localhost:8011"};

        DXTickDB db = TickDBFactory.createFromUrl(args[0]);
        db.open(false);

        LiveCursorWatcher watcher = null;

        try {
            DXTickStream stream = createSampleStream(db);
            watcher = monitorData(stream);
            loadData(stream);

        } finally {
            if (watcher != null)
                watcher.close();
            db.close();
        }
    }
}
