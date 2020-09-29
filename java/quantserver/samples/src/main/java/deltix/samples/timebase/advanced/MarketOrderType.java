package deltix.samples.timebase.advanced;


/**
 *  An order to buy a security at current market prices. 
 */
public final class MarketOrderType extends OrderType {

	public static final String DB_DISCRIMINATOR = "M";
	public static final String NAME = "MKT";

    public static final MarketOrderType INSTANCE = new MarketOrderType(); //TODO: use this in most places
    public MarketOrderType () {}


    @Override
    public String getName() {
        return NAME;
    }

    public String toString () {
        return "MARKET";
    }
}
