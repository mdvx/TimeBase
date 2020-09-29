package deltix.samples.timebase.basics;

import deltix.timebase.messages.InstrumentMessage;

/**
 * POJO class to store Stock Market information like Bars in Timebase stream
 */

public class MyBarMessage extends InstrumentMessage {

    public double closePrice;

    public double openPrice;

    public double highPrice;

    public double lowPrice;

    public double volume;

    public String exchange;

    @Override
    public StringBuilder toString(StringBuilder sb) {
        sb.append("{ \"$type\":  \"MyBarMessage\"");

        sb.append(", \"closePrice\": ").append(closePrice);
        sb.append(", \"openPrice\": ").append(openPrice);
        sb.append(", \"highPrice\": ").append(highPrice);
        sb.append(", \"lowPrice\": ").append(lowPrice);
        sb.append(", \"volume\": ").append(volume);

        return sb;
    }
}
