package deltix.qsrv.testsetup;

import deltix.timebase.messages.InstrumentMessage;

/**
 *
 */
public class FloatMessage extends InstrumentMessage {
    public static final String      TYPE_NAME = FloatMessage.class.getName ();

    public double           data;

    @Override
    public String               toString () {
        return (super.toString () + ",data: " + data);
    }
}
