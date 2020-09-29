package deltix.samples.timebase.advanced;

/**
 *
 */
public class LimitOrder extends OrderType {
    public double       price;
    public double       discretionAmount;
    public CharSequence comment;

    public LimitOrder() {
    }

    public LimitOrder(double price, double discretionAmount, CharSequence comment) {
        this.price = price;
        this.discretionAmount = discretionAmount;
        this.comment = comment;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString () {
        return "LIMIT " + price + "," + discretionAmount + "," + comment;
    }
}
