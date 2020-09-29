using System;

using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.tickdb.pub;
using deltix.util.time;
using System.Collections.Generic;
using deltix.data.stream;
using deltix.qsrv.hf.pub.md;
using deltix.qsrv.hf.stream;
using deltix.util.progress;
using java.lang;

namespace deltix.samples.timebase.smd {



    public class SecuritiesExportSample
    {
 
        private void export (DXTickDB db) {

        SelectionOptions            options = new SelectionOptions ();

        DXTickStream stream = db.getStream ("securities");
        Interval periodicity =  stream.getPeriodicity().getInterval();
        MessageWriter2 writer = MessageWriter2.create (
            new java.io.File("securities.qsmsg.gz"),
            periodicity,
            options.raw ? null : TypeLoaderImpl.DEFAULT_INSTANCE,
            collectTypes (stream)
        );

        try {
            export (stream, writer, options);
        } finally {
            writer.close ();
        }
    }

    private void                export (
        DXTickStream stream,
        MessageChannel dest,
        SelectionOptions options
    )
    {
        long []                 tr = TickTools.getTimeRange (stream);

        if (tr == null) {
            Console.WriteLine("No data in source.");
            return;
        }

        ConsoleProgressIndicator cpi = new ConsoleProgressIndicator ();

        Console.WriteLine ("Copying ...");

        MessageSource cur = stream.select (Long.MIN_VALUE, options, null, null);

        try {
            TickTools.copy (cur, tr, dest, cpi);
        } finally {
            Console.WriteLine();
            cur.close ();
        }
    }

    private RecordClassDescriptor[] collectTypes (TickStream stream) {
        List<RecordClassDescriptor> types = new List<RecordClassDescriptor>();
        if (stream.isFixedType ())
            types.Add (stream.getFixedType ());
            
        else
            types.AddRange(stream.getPolymorphicDescriptors ());
        

        return types.ToArray();
    }
        /**
         *  Runs the sample
         * 
         *  @param args     TimeBase URL. Defaults to <tt>dxtick://localhost:8011</tt>.
         */
        public static void Main (string [] args) {
            SecuritiesExportSample sample = new SecuritiesExportSample();
            DXTickDB tickDB = null;
            try
            {
                tickDB = TickDBFactory.createFromUrl("dxtick://localhost");
                tickDB.open(false);
                sample.export(tickDB);
            }
            finally
            {
                if (tickDB != null && tickDB.isOpen())
                    tickDB.close();
            }
        } 
    }
}
