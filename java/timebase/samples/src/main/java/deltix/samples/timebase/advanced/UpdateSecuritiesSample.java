package deltix.samples.timebase.advanced;

import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.md.Introspector;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.lock.DBLock;
import deltix.qsrv.hf.tickdb.pub.lock.LockType;
import deltix.qsrv.hf.tickdb.pub.lock.StreamLockedException;
import deltix.samples.timebase.BarMessage;
import deltix.timebase.api.messages.InstrumentType;
import deltix.timebase.api.messages.securities.Equity;
import deltix.timebase.api.messages.securities.Future;
import deltix.timebase.api.messages.securities.Index;
import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.IdentityKey;
import deltix.timebase.messages.InstrumentKey;
import deltix.timebase.messages.InstrumentMessage;

import java.security.Security;
import java.util.GregorianCalendar;
import java.util.Hashtable;

/**
 *  <p><b>WARNING</b>: This example clears the <b>securities</b> stream in 
 *  TimeBase. Do not run on production instances!</p>
 * 
 *  <p>This sample illustrates the process of creating and updating the 
 *  <b>securities</b> stream in TimeBase. If the
 *  <b>securities</b> stream does not exist, this program will create it 
 *  programmatically. In particular, this example illustrates the proper use of
 *  locking for transactional updates.</p>
 */
public class UpdateSecuritiesSample {
    public static final String      STREAM_KEY = "securities";

    /**
     *  Creates the <b>securities</b> stream if it does not exist, and inserts
     *  a few sample securities into it.
     * 
     *  @param db  Database connection.
     */
    public static void initStream(DXTickDB db) throws Introspector.IntrospectionException {
        
        DXTickStream stream = db.getStream (STREAM_KEY);
        //
        //  If 'securities' stream does not exist, create it
        //
        if (stream == null) {
            RecordClassDescriptor descriptor = (RecordClassDescriptor) Introspector.introspectSingleClass(Security.class);
            StreamOptions   options = 
                StreamOptions.polymorphic(                
                    StreamScope.DURABLE, STREAM_KEY, null, 1,
                    descriptor
                );

            stream = db.createStream("securities", options);
        }

        //
        //  Load a few sample instruments
        //
        TickLoader      loader = null;
        DBLock          dbLock = null;

        try {
            //
            //  The securities stream should be locked when data is inserted. 
            //  Locking the securities stream achieves two goals:
            //      1) It ensures that no other application can accidentally 
            //          corrupt the data being inserted.
            //      2) Removing the lock also notifies interested applications
            //          about the fact that securities data has been changed.
            //          For instance, QuantOffice reloads symbols, 
            //          Aggregator checks if market data subscription should be
            //          updated, etc.
            //
            dbLock = stream.tryLock(LockType.WRITE, 30 * 1000);
            //
            //  Clear all data from stream - for the purpose of this example.
            //
            //  Warning: do not run on a production instance; this will clear all
            //  securities.
            //
            stream.clear ();
            //
            //  Create loader. We set all timestamps to 0, because QuantOffice 
            //  does not support historical securities information.
            //
            loader = stream.createLoader ();

            Equity equity = new Equity ();
            equity.setSymbol("IBM");
            equity.setExchangeId("XNYS");
            equity.setTimeStampMs(0);
            loader.send (equity);

            equity.setSymbol("MSFT");
            equity.setExchangeId("XNAS");
            equity.setTimeStampMs(0);
            loader.send (equity);

            Future future = new Future();
            future.setSymbol("FVH11");
            future.setContractMonth((byte)2);
            future.setRootSymbol("FV");
            future.setExchangeId("XCBT");
            future.setTimeStampMs(0);
            loader.send(future);

            Index index = new Index();
            index.setSymbol("SPX");
            index.setTimeStampMs(0);
            loader.send(index);
        }
        catch (StreamLockedException ex) {
            System.out.print("Cannot lock 'securities' stream. Error: " + ex);
        }
        finally {
            //
            // Close the loader
            //
            if (loader != null)
                loader.close ();
            //
            // THEN release the lock. This will generate the system event message
            // deltix.qsrv.hf.pub.EventMessage, which notifies interested 
            // parties that the stream has been updated. This message can be 
            // received from system stream 
            // deltix.qsrv.hf.tickdb.pub.TickDBFactory.EVENTS_STREAM_NAME
            //
            if (dbLock != null)
                dbLock.release();
        }
    }

    public static void updateStream(DXTickDB db) {

        DXTickStream stream = db.getStream (STREAM_KEY);
        //
        // General approach: to change security metadata stream, one has to 
        //  rewrite its content. TimeBase does not support in-place update,
        //  and the securities stream is just like any other stream. Thankfully,
        //  the content of the securities stream is relatively small, even if 
        //  it contains on the order of 100K instruments, so it easily fits
        //  in memory. 
        //
        //  We will:
        //      1) load all records into a data collection, 
        //      2) update the collection in memory, and 
        //      3) store it back into TimeBase.
        //
        //  We assume that stream will contains only one record per instrument,
        //  which is currently a good assumption, due to QuantOffice not 
        //  handling historical securities data.
        //
        Hashtable<IdentityKey, InstrumentMessage> cache =
                new Hashtable<>();
        //
        //  Load all data from the stream
        //
        TickCursor cursor = null;

        try {
            cursor = stream.select(Long.MIN_VALUE, new SelectionOptions(), null, (CharSequence[]) null);

            while (cursor.next()) {
                InstrumentMessage message = cursor.getMessage();

                //
                // Put a copy of each message into cache
                //
                InstrumentMessage copy = message.clone();

                // InstrumentMessage is InstrumentIdentity, so we can it as key
                cache.put(new ConstantIdentityKey(message.getSymbol()), copy);
            }
        } finally {
            //
            //  Cursor must be closed
            //
            if (cursor != null)
                cursor.close();
        }
        //
        //  Execute sample updates as required.
        //
        Future future = (Future) cache.get(new InstrumentKey("FVH11"));
        future.setExpirationDate(new GregorianCalendar(2011, 01, 2).getTimeInMillis());

        Equity equity = (Equity) cache.get(new InstrumentKey( "MSFT"));
        //
        //  Add a new equity
        //
        equity = new Equity ();
        equity.setSymbol("ORCL");
        equity.setCurrencyAlphanumeric(ExchangeCodec.codeToLong("USD"));
        equity.setExchangeId("XNYS");
        equity.setTimeStampMs(0);

        cache.put(new ConstantIdentityKey(equity.getSymbol()), equity);
        //
        //  Store the cache back to stream
        //
        TickLoader  loader = null; 
        DBLock      dbLock = null;

        try {
            //
            //  The securities stream should be locked when data is inserted. 
            //  Locking the securities stream achieves two goals:
            //      1) It ensures that no other application can accidentally 
            //          corrupt the data being inserted.
            //      2) Removing the lock also notifies interested applications
            //          about the fact that securities data has been changed.
            //          For instance, QuantOffice reloads symbols, 
            //          Aggregator checks if market data subscription should be
            //          updated, etc.
            //
            dbLock = stream.tryLock(LockType.WRITE, 30 * 1000);
            //
            //  Clear old data from the stream first
            //
            stream.clear ();
            //
            //  Store data
            //
            loader = stream.createLoader ();
            
            for (InstrumentMessage message : cache.values())
                loader.send(message);
        }
        catch (StreamLockedException ex) {
            System.out.print("Cannot lock 'securities' stream. Error: " + ex);
        }
        finally {
            //
            // Close loader
            //
            if (loader != null)
                loader.close ();
            //
            // THEN release the lock. This will generate the system event message 
            // deltix.qsrv.hf.pub.EventMessage, which notifies interested 
            // parties that the stream has been updated. This message can be 
            // received from system stream 
            // deltix.qsrv.hf.tickdb.pub.TickDBFactory.EVENTS_STREAM_NAME
            //
            if (dbLock != null)
                dbLock.release();
        }
    }

    /**
     *  Runs the sample
     * 
     *  @param args     TimeBase URL. Defaults to <tt>dxtick://localhost:8011</tt>.
     */
    public static void      main (String [] args) {
        if (args.length == 0)
            args = new String [] { "dxtick://localhost:8011" };

        DXTickDB    db = TickDBFactory.createFromUrl (args [0]);

        db.open (false);

        try {
            initStream(db);

            updateStream(db);
        } catch (Introspector.IntrospectionException e) {
            e.printStackTrace();
        } finally {
            db.close ();
        }
    } 
}
