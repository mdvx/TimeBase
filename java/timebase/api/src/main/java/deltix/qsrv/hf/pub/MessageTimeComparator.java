package deltix.qsrv.hf.pub;

import deltix.timebase.messages.TimeStampedMessage;
import deltix.util.lang.MathUtil;
import java.util.Comparator;

/**
 *
 */
public class MessageTimeComparator implements Comparator <TimeStampedMessage> {

    private MessageTimeComparator () { }
    
    public int      compare (TimeStampedMessage o1, TimeStampedMessage o2) {
        return (MathUtil.sign (o1.getNanoTime() - o2.getNanoTime()));
    }



    public static final MessageTimeComparator   INSTANCE =
        new MessageTimeComparator ();
}
