using System;
using System.IO;

using java.lang;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.pub.secmd;
using deltix.qsrv.hf.pub.md;
using deltix.qsrv.hf.tickdb.pub;
using System.Reflection;

namespace deltix.samples.timebase.smd {
	/// <summary>
	/// This sample illustrates the process of loading security metadata into the
	/// "securities" stream, when there are custom columns. To prepare for running
	/// this test:
	/// <ol>
	///   <li>Create the default securities stream in TimeBase Administrator</li>
	///   <li>Edit the schema</li>
	///   <li>Add a new class called deltix.samples.timebase.smd.CustomEquity as a subclass of Equity.</li>
	///   <li>Add a new String field called <b>BBG_id</b> to CustomEquity.</li>
	///   <li>Accept changes.</li>
	/// </ol>
	/// Refer to the following screen shot:
	/// <p>
	///     <img src="deltix/samples/timebase/smd/NewClassScreenShot.jpg"/>
	/// </p>
	/// </summary>
	/// 

	public class CustomEquity : Equity {
		public string               BBG_id;
    }

	public class CustomEquityLoadSample {
        public static readonly string      STREAM_KEY = "securities";        

        public static void loadCustomEquities(DXTickDB db) {
            DXTickStream            stream = db.getStream (STREAM_KEY);

            CustomEquity            customEquity = new CustomEquity ();

            LoadingOptions          options = new LoadingOptions ();

            //
            //  Optionally, clear all data from stream
            //
            stream.clear ();
            //
            //  Load some extended equities
            //
            TickLoader              loader = stream.createLoader (options);

            try {
                customEquity.symbol = "IBM";
                customEquity.exchangeCode = "XNYS";
                customEquity.BBG_id = "IBM UN Equity";
                loader.send (customEquity);

                customEquity.symbol = "MSFT";
                customEquity.exchangeCode = "XNAS";
                customEquity.BBG_id = "MSFT UW Equity";
                loader.send (customEquity);

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
                loadCustomEquities (db);
            } finally {
                db.close ();
            }
        }
    }
}
