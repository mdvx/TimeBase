package deltix.qsrv.hf.tickdb.pub.channel;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.stream.MessageSorter;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.streaming.MessageChannel;
import deltix.streaming.MessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.lang.Util;
import java.io.IOException;

/**
 *
 */
public final class GlobalSortChannel
    implements MessageChannel<InstrumentMessage>
{
    private final MessageSorter                         sorter;
    private final MessageChannel <InstrumentMessage>    downstream;

    // stats for getProgress function
    private volatile long           num = 0;

    public GlobalSortChannel (
        long                                memory,
        MessageChannel <InstrumentMessage>  downstream,
        LoadingOptions                      options,
        RecordClassDescriptor ...           descriptors
    )
    {
        this.downstream = downstream;
        
        sorter = 
            new MessageSorter (
                memory,
                options.raw ? null : options.getTypeLoader (),
                descriptors
            );
    }

    public void                 send (InstrumentMessage msg) {
        try {
            sorter.add (msg);
        } catch (IOException e) {
            throw new deltix.util.io.UncheckedIOException (e);
        }
    }

    public void                 close () {
        MessageSource<InstrumentMessage> cur;

        try {
            cur = sorter.finish (null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            while (cur.next ()) {
                downstream.send (cur.getMessage ());
                num++;
            }
        } finally {
            Util.close (cur);
            Util.close (downstream);
            sorter.close ();
        }
    }

    public double               getProgress () {
        return (double) num / sorter.getTotalNumMessages ();
    }
}
