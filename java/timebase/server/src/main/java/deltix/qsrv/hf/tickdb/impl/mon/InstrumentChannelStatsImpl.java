package deltix.qsrv.hf.tickdb.impl.mon;

import deltix.qsrv.hf.tickdb.pub.mon.*;
import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.IdentityKey;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.time.TimeKeeper;
import java.util.Date;

/**
 *
 */
public class InstrumentChannelStatsImpl
    extends ConstantIdentityKey
    implements InstrumentChannelStats
{
    private long                    numMessages = 0;
    private long                    lastTimestamp = Long.MIN_VALUE;
    private long                    lastSysTime = Long.MIN_VALUE;

    public InstrumentChannelStatsImpl (InstrumentChannelStatsImpl copy) {
        super (copy.getSymbol());

        numMessages = copy.numMessages;
        lastTimestamp = copy.lastTimestamp;
        lastSysTime = copy.lastSysTime;
    }

    public InstrumentChannelStatsImpl (IdentityKey copy) {
        super (copy.getSymbol());
    }

    public void             register (InstrumentMessage msg) {
        lastTimestamp = msg.getTimeStampMs();
        numMessages++;
        lastSysTime = TimeKeeper.currentTime;
    }

    public long             getLastMessageSysTime () {
        return (lastSysTime);
    }

    public long             getLastMessageTimestamp () {
        return (lastTimestamp);
    }

    public Date                         getLastMessageDate () {
        return lastTimestamp != Long.MIN_VALUE ? new Date (lastTimestamp) : null;
    }

    public Date                         getLastMessageSysDate () {
        return lastSysTime != Long.MIN_VALUE ? new Date (lastSysTime) : null;
    }

    public long             getTotalNumMessages () {
        return (numMessages);
    }
}
