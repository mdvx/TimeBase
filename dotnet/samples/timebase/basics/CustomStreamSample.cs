using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.pub.codec;
using deltix.qsrv.hf.pub.md;
using deltix.qsrv.hf.tickdb.pub;
using deltix.util.memory;
using deltix.util.time;

namespace deltix.samples.timebase.basics {
	public class CustomStreamSample {
	    public static readonly string      STREAM_KEY = "custom.stream";
	
	    public static RecordClassDescriptor   CUSTOM_CLASS =
	        new RecordClassDescriptor (
	            "MyClass",
	            "My Custom Class Title",
	            false,
	            null,
	            new NonStaticDataField (
	                "price",
	                "Price (FLOAT)",
	                new FloatDataType (FloatDataType.ENCODING_FIXED_DOUBLE, true),
	                null
	            ),
	            new NonStaticDataField (
	                "count",
	                "Count (INTEGER)",
	                new IntegerDataType (IntegerDataType.ENCODING_INT32, true),
	                null
	            ),
	            new NonStaticDataField (
	                "description",
	                "Description (VARCHAR)",
	                new VarcharDataType (VarcharDataType.ENCODING_INLINE_VARSIZE, true, true),
	                null
	            ),
	            new NonStaticDataField (
	                "dueDate",
	                "Due Date (DATE)",
	                new DateTimeDataType (true),
	                null
	            ),
	            new NonStaticDataField (
	                "accepted",
	                "Accepted (BOOLEAN)",
	                new BooleanDataType (true),
	                null
	            )
	        );
	
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
	
	            stream.setFixedType (CUSTOM_CLASS);
	        }
	    }
	
	    public static void      readData (DXTickDB db) {
	        DXTickStream            stream = db.getStream (STREAM_KEY);
	        RecordClassDescriptor   classDescriptor = stream.getFixedType ();
	        //
			//  Always use raw = true for custom messages.
			//
			SelectionOptions        options = new SelectionOptions (true, false);

			//
			//  List of entities to subscribe (if null, all stream entities will be used)
			//
			InstrumentIdentity[] 	entities = null;

			//
			//  List of types to subscribe - seelect only "MyClass" messages
			//
			String[] 				types = new String[] { "MyClass" };
	        
	        //
	        //  Cursor is equivalent to a JDBC ResultSet
	        //
	        TickCursor              cursor = stream.select (long.MinValue, options, types, entities);
			
	        MemoryDataInput         input = new MemoryDataInput ();
	        UnboundDecoder          decoder =
	            CodecFactory.COMPILED.createFixedUnboundDecoder (classDescriptor);
	
	        try {
	            while (cursor.next ()) {
	                //
	                //  We can safely cast to RawMessage because we have requested
	                //  a raw message cursor.
	                //
	                RawMessage      msg = (RawMessage) cursor.getMessage ();
	                //
	                //  Print out standard fields
	                //
	                Console.Write (
	                	DateConverter.FromLong (msg.timestamp) + " " +
	                	msg.symbol.toString () + " " +
	                    msg.instrumentType
	                );
	                //
	                //  Iterate over custom fields.
	                //
	                input.setBytes (msg.data, msg.offset, msg.length);
	                decoder.beginRead (input);
	
	                while (decoder.nextField ()) {
	                    NonStaticFieldInfo  df = decoder.getField ();
	                    //
	                    //  Any data type can be retrieved via getString ()
	                    //  but in most cases specific get... methods should be
	                    //  invoked, e.g. getLong (), getDouble (), etc.
	                    //
	                    Console.Write (", " + df.getName () + ": " + decoder.getString ());
	                }
	
	                Console.WriteLine ();
	            }
	        } finally {
	            cursor.close ();
	        }
	    }
	
	    public static void      loadData (DXTickDB db) {
	        DXTickStream            stream = db.getStream (STREAM_KEY);
	        RecordClassDescriptor   classDescriptor = stream.getFixedType ();
	        RawMessage              msg = new RawMessage (classDescriptor);
	
	        //  Always use raw = true for custom messages.
	        LoadingOptions          options = new LoadingOptions (true);
	        TickLoader              loader = stream.createLoader (options);
	
	        //  Re-usable buffer for collecting the encoded message
	        MemoryDataOutput        dataOutput = new MemoryDataOutput ();
	        FixedUnboundEncoder     encoder =
	            CodecFactory.COMPILED.createFixedUnboundEncoder (classDescriptor);
	
	        try {
	            //  Generate a few messages
	            for (int ii = 1; ii < 100; ii++) {
	                //
	                //  Set up standard fields
	                //
	                msg.timestamp = DateConverter.ToLong (DateTime.Now);
	                msg.symbol = "AAPL";
	                msg.instrumentType = InstrumentType.EQUITY;
	                //
	                //  Set up custom fields
	                //
	                dataOutput.reset ();
	                encoder.beginWrite (dataOutput);
	                //
	                //  Fields must be set in the order the encoder
	                //  expects them, which in the case of a fixed-type stream
	                //  with a non-inherited class descriptor is equivalent to the
	                //  order of the class descriptor's fields.
	                //
	                encoder.nextField ();
	                encoder.writeDouble (ii * 0.25);
	
	                encoder.nextField ();   // count
	                encoder.writeInt (ii);
	
	                encoder.nextField ();   // description
	                encoder.writeString ("Message #" + ii);
	
	                encoder.nextField ();   // dueDate
	                encoder.writeLong (msg.timestamp + 864000000L); // add 10 days
	
	                encoder.nextField ();   // accepted
	                encoder.writeBoolean (ii % 2 == 0);
	
	                if (encoder.nextField ())   // make sure we are at end
	                    throw new Exception ("unexpected field: " + encoder.getField ());
	
	                msg.setBytes (dataOutput, 0);
	
	                loader.send (msg);
	            }
	        } finally {
	            loader.close ();
	        }
	    }
	
	    public static void      Main (string [] args) {
	        if (args.Length == 0)
	            args = new string [] { "dxtick://localhost:8011" };
	
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
}
