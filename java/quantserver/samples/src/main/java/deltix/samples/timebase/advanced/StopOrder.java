package deltix.samples.timebase.advanced;

/**
 *
 */
public class StopOrder extends OrderType {
    public double stopPrice;

    public StopOrder() {
    }

    public StopOrder(double stopPrice) {
        this.stopPrice = stopPrice;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "STOP " + stopPrice;
    }

}
