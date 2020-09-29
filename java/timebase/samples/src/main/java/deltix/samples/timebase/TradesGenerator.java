package deltix.samples.timebase;

import deltix.timebase.messages.InstrumentMessage;

import java.util.GregorianCalendar;
import java.util.Random;

public class TradesGenerator extends BaseGenerator<InstrumentMessage>
{
    private Random rnd = new Random();
    private int count;

    public TradesGenerator(GregorianCalendar calendar, int size, int count, String ... tickers) {
        super(calendar, size, tickers);
        this.count = count;
    }

    public boolean next() {
        if (isAtEnd())
            return false;

        deltix.timebase.api.messages.TradeMessage message = new deltix.timebase.api.messages.TradeMessage();
        message.setSymbol(symbols.next());
        message.setTimeStampMs(symbols.isReseted() ? getNextTime() : getActualTime());

        message.setPrice(rnd.nextDouble()*100);
        message.setSize(rnd.nextInt(1000));
        message.setCurrencyCode((short)840);

        current = message;
        count--;
        return true;
    }

    public boolean isAtEnd() {
        return count == 0;
    }
}
