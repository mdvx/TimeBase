package deltix.samples.timebase.process.demos;

import deltix.timebase.api.messages.InstrumentMessage;
import deltix.samples.timebase.process.pub.*;
import deltix.util.time.*;
import java.util.*;
import java.text.*;

/**
 *  Finds gaps in data larger than a specified threshold, within the same day 
 *  only.
 */
public class IntradayDataGapFinder extends AbstractMessageProcessor {
    //
    //  Configuration
    //
    private long            minGap = 600000;    // 10 min
    private TimeZone        timeZone = null;
    private int             openTime = 0;
    private int             closeTime = 85399999;
    
    public void             setMinGap (String value) {
        minGap = TimeFormatter.parseTimeOfDay (value, 1000);
        
        if (minGap < 1000)
            throw new IllegalArgumentException ("minGap value too small: " + value);
        
        if (minGap > 36000000)
            throw new IllegalArgumentException ("minGap value too large: " + value);        
    }

    public void             setTimeZone (String tzname) {
        timeZone = TimeZone.getTimeZone (tzname);
        
        if (timeZone == null)
            throw new IllegalArgumentException ("Time zone not found: " + tzname);
    }        
    
    public void             setOpenTime (String time) {
        openTime = (int) TimeFormatter.parseTimeOfDay (time, 1000);
    }
    
    public void             setCloseTime (String time) {
        closeTime = (int) TimeFormatter.parseTimeOfDay (time, 1000);
    }
    //
    //  Processing code
    //
    private long            lastTimestamp;
    private DateFormat      df;
    
    @Override
    public void             init (ProcessingJob job) {
        if (timeZone == null)
            throw new IllegalStateException (
                "Required timeZone property has not been set"
            );
        
        if (openTime >= closeTime)
            throw new IllegalStateException (
                "Illegal open/close time range: " + 
                TimeFormatter.formatTimeofDayMillis (openTime) +
                " .. " + TimeFormatter.formatTimeofDayMillis (closeTime)
            );
        
        lastTimestamp = Long.MIN_VALUE;  
        
        df = new SimpleDateFormat ("yyyy-MM-dd");
        df.setTimeZone (timeZone);
    }
            
    @Override
    public boolean          process (InstrumentMessage msg) {        
        checkGap (msg);        
        lastTimestamp = msg.getTimeStampMs();
        return (true);
    }    
    
    private void            checkGap (InstrumentMessage msg) {
        if (lastTimestamp == Long.MIN_VALUE) 
            return;
        
        long                    newTimestamp = msg.getTimeStampMs();
        long                    gap = newTimestamp - lastTimestamp;
        //
        //  Check against minGap
        //
        if (gap < minGap) 
            return;        
        //
        //  Only report intraday gaps
        //  Quick test
        //
        if (gap >= 86400000)
            return;
        //
        //  Correct (but slower) test:
        //
        long                    midnight = TimeZoneUtils.getMidnightOn (timeZone, lastTimestamp);

        if (newTimestamp - midnight >= 86400000)
            return;
        //
        //  Ignore times outside of set range.        
        //
        int                     lastTimeOfDay = (int) (lastTimestamp - midnight);
        
        if (lastTimeOfDay < openTime || lastTimeOfDay > closeTime)
            return;
        
        int                     newTimeOfDay = (int) (newTimestamp - midnight);
        //
        //  Assert timestamp ordering; this should never, ever trip!
        //
        if (newTimeOfDay < lastTimeOfDay)   
            throw new AssertionError (
                "logic error; " + lastTimestamp + " -> " + newTimestamp
            );
        //
        //  No need to compare newTimeOfDay against openTime.
        //
        if (newTimeOfDay > closeTime)
            return;
        
        System.out.println (
            msg.getSymbol() + ": Gap found on " + df.format (lastTimestamp) +
            ": " + TimeFormatter.formatTimeofDayMillis (lastTimeOfDay) +
            " .. " + TimeFormatter.formatTimeofDayMillis (newTimeOfDay)
        );        
    }
}
