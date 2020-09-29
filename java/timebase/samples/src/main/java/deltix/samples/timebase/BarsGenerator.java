package deltix.samples.timebase;

import deltix.timebase.api.messages.InstrumentType;
import deltix.timebase.messages.InstrumentMessage;

import java.util.GregorianCalendar;
import java.util.Random;

public class BarsGenerator extends BaseGenerator<InstrumentMessage>
{
    private Random rnd = new Random();
    private int count;

    public InstrumentType instrumentType = InstrumentType.EQUITY;

    public BarsGenerator(GregorianCalendar calendar, int interval, int count, String ... tickers) {
        super(calendar, interval, tickers);
        this.count = count;
    }

    public boolean next() {
        if (isAtEnd())
            return false;

        deltix.timebase.api.messages.BarMessage message = new deltix.timebase.api.messages.BarMessage();
        message.setSymbol(symbols.next());
        message.setTimeStampMs(symbols.isReseted() ? getNextTime() : getActualTime());

        message.setHigh(getDouble(100, 6));
        message.setOpen(message.getHigh() - getDouble(10, 6));
        message.setClose(message.getHigh() - getDouble(10, 6));
        message.setLow(Math.min(message.getOpen(), message.getClose()) + getDouble(10, 6));
        message.setVolume(count);
        message.setCurrencyCode((short)840);

        current = message;
        count--;
        return true;
    }

    public double getDouble(int max, int precision) {
        return (int)(rnd.nextDouble() * Math.pow(10, max + precision)) / Math.pow(10, precision);
    }

    public boolean isAtEnd() {
        return count == 0;
    }
}
