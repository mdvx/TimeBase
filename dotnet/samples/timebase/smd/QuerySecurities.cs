using System;
using System.IO;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.pub.md;
using deltix.qsrv.hf.pub.secmd;
using deltix.qsrv.hf.tickdb.pub;
using deltix.qsrv.hf.tickdb.pub.query;


namespace deltix.samples.timebase.smd {
	/// This sample illustrates the process of querying security metadata while
	/// applying QQL (QuantServer Query Language) filters. To prepare for running
	/// this test, start TimeBase with the sample database (available as an
	/// installation package).
    public class QuerySecurities {
		class QueryResult : InstrumentMessage {
            public string               name;
        }

        ///
        /// List futures names, whose root symbol equals "ES". The string literal
        /// "ES" is passed inline in the where clause of the select statement.
        /// The result is received using the Bound API, i.e. in the form of native
        /// C# objects.       
        public static void listFuturesNamesByRootSymbolInline (DXTickDB db) {
            Console.WriteLine ("listFuturesNamesByRootSymbolInline:");

            SelectionOptions            options = new SelectionOptions ();

            options.typeLoader = new SimpleTypeLoader (null, typeof (QueryResult));

            InstrumentMessageSource     cursor =
                db.executeQuery (
                    "select name from securities where rootsymbol = 'ES'",
                    options
                );

            try {
                while (cursor.next ()) {
                    QueryResult         msg = (QueryResult) cursor.getMessage ();

                    Console.WriteLine ("    symbol: " + msg.symbol + " name: " +  msg.name);
                }
            } finally {
                cursor.close ();
            }
        }

        /// List futures names, whose root symbol equals "NQ". The string literal
        /// "ES" is passed as a query parameter. This allows cleaner code,
        /// avoids the necessity to escape special characters, such as quotes, and
        /// allows the query engine to cache the statement.
        /// The result is received using the Bound API, i.e. in the form of native
        /// C# objects.        
        public static void listFuturesNamesByRootSymbolParam (DXTickDB db, string root) {
            Console.WriteLine ("listFuturesNamesByRootSymbolParam (" + root + "):");

            SelectionOptions            options = new SelectionOptions ();

            options.typeLoader = new SimpleTypeLoader (null, typeof (QueryResult));

            Parameter                   rootSymbolParam =
                new Parameter ("rootSymbolParam", StandardTypes.CLEAN_VARCHAR);

            rootSymbolParam.value.writeString (root);

            InstrumentMessageSource     cursor =
                db.executeQuery (
                    "select name from securities where rootsymbol = rootSymbolParam",
                    options,
                    rootSymbolParam
                );

            try {
                while (cursor.next ()) {
                    QueryResult         msg = (QueryResult) cursor.getMessage ();

                    Console.WriteLine ("    symbol: " + msg.symbol + " name: " +  msg.name);
                }
            } finally {
                cursor.close ();
            }
        }

        /// Find a specific security object by symbol and return it in its entirety,
        /// as opposed to a subset of its fields. This is the effect of <tt>select *</tt>.
        /// The result is received using the Bound API, i.e. in the form of a
        /// native C# object.
        public static void listFuturesBySymbol (DXTickDB db, string symbol) {
            Console.WriteLine ("listFuturesBySymbol (" + symbol + "):");

            Parameter                   symbolParam =
                new Parameter ("symbolParam", StandardTypes.CLEAN_VARCHAR);

            symbolParam.value.writeString (symbol);

            InstrumentMessageSource     cursor =
                db.executeQuery (
                    "select * from securities where symbol = symbolParam",
                    symbolParam
                );

            try {
                while (cursor.next ()) {
                    GenericInstrument         msg = (GenericInstrument) cursor.getMessage ();

                    Console.WriteLine ("    " + msg);
                }
            } finally {
                cursor.close ();
            }
        }

        public static void Main (string [] args) {
            if (args.Length == 0)
                args = new string [] { "dxtick://localhost:8011" };

            DXTickDB    db = TickDBFactory.createFromUrl (args [0]);

            db.open (true);

            try {
                listFuturesNamesByRootSymbolInline (db);
                listFuturesNamesByRootSymbolParam (db, "NQ");
                listFuturesBySymbol (db, "ESZ11");
            } finally {
                db.close ();
            }
        }
    }
}
