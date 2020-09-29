using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.pub.util;
using deltix.qsrv.hf.pub.secmd;
using deltix.qsrv.hf.tickdb.pub;
using deltix.qsrv.hf.tickdb.pub.@lock;
using deltix.util.currency;
using deltix.util.time;
using deltix.qsrv.hf.blocks;
using System.Collections;
using System.Collections.Generic;	
	 
namespace deltix.samples.timebase.smd {

	/// <summary>
	/// <p><b>WARNING</b>: This example clears the <b>securities</b> stream in 
    /// TimeBase. Do not run on production instances!</p>
    /// 
    /// <p>This sample illustrates the process of creating and updating the 
    /// <b>securities</b> stream in TimeBase. If the
    /// <b>securities</b> stream does not exist, this program will create it 
    /// programmatically. In particular, this example illustrates the proper use of
    /// locking for transactional updates.</p>
	/// </summary>

	// Helper classs
	public class MessageKey : InstrumentKey, IComparable<InstrumentIdentity>
	{
		public MessageKey(InstrumentType type, String symbol) : base(type, symbol)
		{ }

		public MessageKey(InstrumentMessage msg) : base(msg)
		{ }

		int IComparable<InstrumentIdentity>.CompareTo(InstrumentIdentity other)
		{
			return this.compareTo(other);
		}		
	}

	public class ChangeListener : LiveCursorWatcher.MessageListener
	{
		private LiveCursorWatcher watcher;
		private TickCursor cursor;

		public ChangeListener(DXTickDB tickdb)
		{
			DXTickStream events = tickdb.getStream(TickDBFactory.EVENTS_STREAM_NAME);

			cursor = events.select(deltix.qsrv.hf.tickdb.pub.TimeConstants.USE_CURRENT_TIME, 
					new SelectionOptions(false, true),
					null,
					new InstrumentIdentity[] { new ConstantInstrumentKey(InstrumentType.STREAM, "securities") });
			this.watcher = new LiveCursorWatcher(cursor, this);
		}
		
		public void onMessage(InstrumentMessage im)
		{
			System.Diagnostics.Debug.WriteLine("'securities' stream updated: {0}", im);
		}

		public void Stop()
		{
			if (watcher != null)
				watcher.close();
			if (cursor != null)
				cursor.close();
		}
		
	}

    public class UpdateSecuritiesSample {
        public static String      STREAM_KEY = "securities";

        /**
         *  Creates the <b>securities</b> stream if it does not exist, and inserts
         *  a few sample securities into it.
         * 
         *  @param db  Database connection.
         */
        public static void initStream(DXTickDB db) {  

            DXTickStream stream = db.getStream (STREAM_KEY);
            //
            //  If 'securities' stream does not exist, create it
            //
            if (stream == null) {
                StreamOptions   options = 
                    StreamOptions.polymorphic(                
                        StreamScope.DURABLE, STREAM_KEY, null, 1,
                        StreamConfigurationHelper.mkSecurityMetaInfoDescriptors()
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
                equity.symbol = "IBM";
                equity.exchangeCode = "XNYS";
                equity.timestamp = 0;
                loader.send (equity);

                equity.symbol = "MSFT";
                equity.exchangeCode = "XNAS";
                equity.timestamp = 0;
                loader.send (equity);

                Future future = new Future();
                future.symbol = "FVH11";
                future.contractMonth = 2;
                future.rootSymbol = "FV";
                future.exchangeCode = "XCBT";
                future.timestamp = 0;
                loader.send(future);

                Index index = new Index();
                index.symbol = "SPX";
                index.timestamp = 0;
                loader.send(index);
            }
            catch (StreamLockedException ex) {
                Console.Write("Cannot lock 'securities' stream. Error: " + ex);
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

			Hashtable cache = new Hashtable();
            //
            //  Load all data from the stream
            //
            TickCursor cursor = null;

            try {
                cursor = stream.select(long.MinValue, new SelectionOptions(), null, null);

                while (cursor.next()) {
                    InstrumentMessage message = cursor.getMessage();

                    //
                    // Put a copy of each message into cache
                    //
                    InstrumentMessage copy = message.copy(true);

                    // InstrumentMessage is InstrumentIdentity, so we can it as key
					MessageKey key = new MessageKey(message);
					cache.Add(key, copy);
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
			Future future = (Future) cache[new MessageKey(InstrumentType.FUTURE, "FVH11")];
            future.expirationDate = DateConverter.ToLong(new DateTime(2011, 3, 31));

            Equity equity = (Equity) cache[new MessageKey(InstrumentType.EQUITY, "MSFT")];
            equity.currencyCode = CurrencyCodeList.getInfoBySymbolic("USD").numericCode;

            //
            //  Add a new equity
            //
            equity = new Equity ();
            equity.symbol = "ORCL";
            equity.currencyCode = CurrencyCodeList.getInfoBySymbolic("USD").numericCode;
            equity.exchangeCode = "XNYS";
            equity.timestamp = 0;

			cache.Add(new MessageKey(equity), equity);
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

                foreach (InstrumentMessage message in cache.Values)
                    loader.send(message);
            }
            catch (StreamLockedException ex) {
                Console.Write("Cannot lock 'securities' stream. Error: " + ex);
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
        public static void Main (string [] args) {
            if (args.Length == 0)
                args = new string [] { "dxtick://localhost:8011" };

            DXTickDB    db = TickDBFactory.createFromUrl (args [0]);

            db.open (false);

			ChangeListener listener = new ChangeListener(db);

            try {
                initStream(db);

                updateStream(db);
            } finally {
				listener.Stop();

                db.close ();
            }
        } 
    }
}
