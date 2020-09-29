package deltix.qsrv.hf.blocks;

import deltix.streaming.MessageChannel;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.BumpUpMessageException;

 /**
 * Provides message processing rule that bump-up out-of-sequence messages. 
 */
public class BumpUpSorter extends AbstractSorter<TimeIdentity> {
    private long            maxDiscrepancy;
    
    public BumpUpSorter(TimeIdentity id, MessageChannel<InstrumentMessage> channel, long maxDiscrepancy) {
        super(channel);
        this.entry = id;
        this.maxDiscrepancy = maxDiscrepancy;
    }

    public void send(InstrumentMessage msg) {
        TimeIdentity key = getEntry(msg);
        final long timestamp = key.getTime();
        if (timestamp > msg.getTimeStampMs() && timestamp - msg.getTimeStampMs() <= maxDiscrepancy) {
            if (!ignoreErrors)
                onError(new BumpUpMessageException(msg, name, timestamp));
            msg.setTimeStampMs(timestamp);
        }
        prev.send(msg);
        key.setTime(msg.getTimeStampMs());
    }
}
