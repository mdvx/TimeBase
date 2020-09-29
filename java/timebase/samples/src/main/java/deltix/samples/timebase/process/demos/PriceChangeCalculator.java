package deltix.samples.timebase.process.demos;

import java.text.SimpleDateFormat;

import deltix.timebase.api.messages.IdentityKey;
import deltix.timebase.api.messages.InstrumentMessage;

import deltix.util.time.*;
import deltix.qsrv.hf.pub.*;
import deltix.samples.timebase.process.pub.*;
import deltix.util.lang.*;

import java.text.ParseException;
import java.util.*;

import static deltix.samples.timebase.process.part.SpecificSignalJobAllocator.*;

/**
 *
 */
public class PriceChangeCalculator extends AbstractMessageProcessor {
    private static class PCCSignal implements Signal {
        private final long                  startTime;
        private final long                  endTime;
        private final ConstantIdentityKey key;
        private final String                line;

        private PCCSignal (
            long                    startTime, 
            long                    endTime,
            IdentityKey key,
            String                  line
        )
        {
            this.startTime = startTime;
            this.endTime = endTime;
            this.key = ConstantIdentityKey.makeImmutable (key);
            this.line = line;
        }
                
        @Override
        public long                     getEndTime () {
            return (endTime);
        }

        @Override
        public IdentityKey getKey () {
            return (key);
        }

        @Override
        public long                     getStartTime () {
            return (startTime);
        }        
    }
        
    static class FuturePoint {
        final int                   originalIdx;
        final Interval              interval;
        long                        triggerTime;
        double                      price;
        long                        actualPriceTime;

        public FuturePoint (int originalIdx, Interval interval) {
            this.originalIdx = originalIdx;
            this.interval = interval;
        }

        private void                computeTriggerTime (Calendar cal, long startTime) {
            if (interval instanceof FixedInterval) {
                FixedInterval       fi = (FixedInterval) interval;
                
                triggerTime = startTime + fi.getSizeInMilliseconds ();
            }
            else {
                MonthlyInterval     mi = (MonthlyInterval) interval;
                
                cal.setTimeInMillis (startTime);                
                cal.add (Calendar.MONTH, mi.getNumberOfMonths ());
                
                triggerTime = cal.getTimeInMillis ();
            }
        }
    }
    
    private static final Comparator <FuturePoint>   BY_TIME = 
        new Comparator <FuturePoint> () {
            @Override
            public int compare (FuturePoint o1, FuturePoint o2) {
                return (MathUtil.compare (o1.triggerTime, o2.triggerTime));
            }        
        };
    
    private static final Comparator <FuturePoint>   BY_INDEX = 
        new Comparator <FuturePoint> () {
            @Override
            public int compare (FuturePoint o1, FuturePoint o2) {
                return (o1.originalIdx - o2.originalIdx);
            }        
        };    
    
    public static class Factory implements MessageProcessorFactory {
        private TimeZone                timeZone = GMT.TZ;
        private Interval []             intervals;
        private String                  dateFormat = "yyyy-MM-dd HH:mm:ss.S";
        private long                    totalTimeRange = 0;
        
        public void     setDateFormat (String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public void     setIntervals (Interval ... intervals) {
            this.intervals = intervals;
            
            for (Interval iv : intervals) {
                long        ivEstimate;
                
                if (iv instanceof FixedInterval)
                    ivEstimate = ((FixedInterval) iv).getSizeInMilliseconds ();
                else
                    ivEstimate = ((MonthlyInterval) iv).getNumberOfMonths () * 31 * 86400000L;
                                
                if (ivEstimate > totalTimeRange)
                    totalTimeRange = ivEstimate;
            }
            
            // Double total range to account for some latency
            totalTimeRange *= 2;
        }

        public void     setIntervals (String ... intervals) {
            int         num = intervals.length;            
            Interval [] arg = new Interval [num];
            
            for (int ii = 0; ii < num; ii++) 
                arg [ii] = Interval.valueOf (intervals [ii]);
                
            setIntervals (arg);
        }

        public void     setTimeZone (TimeZone timeZone) {
            this.timeZone = timeZone;
        }
        
        public void     setTimeZone (String tzName) {
            this.timeZone = TimeZone.getTimeZone (tzName);
        }
                
        @Override
        public MessageProcessor newProcessor () {
            return (new PriceChangeCalculator (timeZone, dateFormat, intervals));
        }        
        
        public long     parseTime (String format, String text) {
            SimpleDateFormat    df = new SimpleDateFormat (format);
            
            df.setTimeZone (timeZone);
            
            try {
                return (df.parse (text).getTime ());
            } catch (ParseException x) {
                throw new IllegalArgumentException (x);
            }
        }
        
        public Signal   createSignal (IdentityKey id, long time, String line) {
            return (new PCCSignal (time, time + totalTimeRange, id, line));
        }
        
        public Signal   createSignal (
            InstrumentType instrumentType,
            String          symbol, 
            String          timeFormat,
            String          time, 
            String          line
        )
        {
            return (
                createSignal (
                    new ConstantIdentityKey (instrumentType, symbol),
                    parseTime (timeFormat, time),
                    line
                )
            );
        }
    }
    
    private final TimeZone              timeZone;
    private final String                dateFormat;
    private final FuturePoint []        points;

    private PriceChangeCalculator (
        TimeZone                    timeZone,
        String                      dateFormat,
        Interval []                 intervals
    )
    {
        this.timeZone = timeZone;
        this.dateFormat = dateFormat;
        
        int                     num = intervals.length;
        
        points = new FuturePoint [num + 1];
        
        points [0] = new FuturePoint (-1, new FixedInterval (0));
        
        for (int ii = 0; ii < num; ii++) 
            points [ii + 1] = new FuturePoint (ii, intervals [ii]);                   
    }    
    //
    //  Processing logic
    //
    private PCCSignal                   csvSignal;
    private int                         intervalIdx = 0;
    
    @Override
    public void             init (ProcessingJob job) {
        if (points == null)
            throw new IllegalStateException ("Intervals are not set");
        
        SignalJob               sjob = (SignalJob) job;
        
        csvSignal = (PCCSignal) sjob.signal; 
        
        long                    startTime = csvSignal.startTime;
        
        Calendar                cal = null;
        //
        //  Only allocate the calendar if we have monthly intervals
        //
        for (FuturePoint fp : points) {
            if (fp.interval instanceof MonthlyInterval) {
                if (timeZone == null)
                    throw new IllegalStateException ("Time zone is not set");
                
                cal = Calendar.getInstance (timeZone);
                break;
            }
        }
        
        for (FuturePoint fp : points) 
            fp.computeTriggerTime (cal, startTime);
                
        Arrays.sort (points, BY_TIME);
        intervalIdx = 0;
    }

    private static double   getPrice (InstrumentMessage msg) {
        if (msg instanceof deltix.timebase.api.messages.TradeMessage)
            return (((deltix.timebase.api.messages.TradeMessage) msg).getPrice());
        
        if (msg instanceof deltix.timebase.api.messages.BarMessage)
            return (((deltix.timebase.api.messages.BarMessage) msg).getClose());
        
        return (Double.NaN);
    }
    
    @Override
    public boolean          process (InstrumentMessage msg) {
        final double            msgPrice = getPrice (msg);
        
        if (Double.isNaN (msgPrice)) 
            return (true);        
        //
        //  See if we have reached triogger times for any intervals.
        //
        final long              msgTime = msg.getTimeStampMs();
        
        for (;;) {
            final FuturePoint   fp = points [intervalIdx];
            final long          triggerTime = fp.triggerTime;

            if (msgTime < triggerTime)
                return (true);

            fp.price = msgPrice;
            fp.actualPriceTime = msgTime;
            
            intervalIdx++;
            
            if (intervalIdx == points.length)
                return (false);
        }        
    }
    
    @Override
    public void             close () {
        Arrays.sort (points, BY_INDEX);
        
        SimpleDateFormat    df = new SimpleDateFormat (dateFormat);
        
        df.setTimeZone (timeZone);
        
        synchronized (System.out) {
            System.out.print (csvSignal.line);
            
            for (int ii = 0; ii < points.length; ii++) {
                if (ii < intervalIdx) {
                    FuturePoint fp = points [ii];
                    Date        timeObject = new Date (fp.actualPriceTime);
                    String      timeString = df.format (timeObject);

                    System.out.print ("," + fp.price + "," + timeString);
                }
                else
                    System.out.print (",,");
            }

            System.out.println ();
        }
    }   
}
