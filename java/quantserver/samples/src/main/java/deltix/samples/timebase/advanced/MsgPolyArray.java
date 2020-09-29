package deltix.samples.timebase.advanced;

import deltix.timebase.api.messages.InstrumentMessage;
import deltix.timebase.api.SchemaArrayType;
import deltix.util.collections.generated.ObjectArrayList;

/**
 *
 */
public class MsgPolyArray extends InstrumentMessage {
    public CharSequence requestId;

    @SchemaArrayType(
            elementTypes = {MarketOrderType.class, LimitOrder.class, StopOrder.class}
    )
    public ObjectArrayList<OrderType> poly;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(requestId).append(",poly: [");
        if(poly != null)
            for (OrderType orderType : poly) {
                sb.append("[");
                sb.append(orderType);
                sb.append("],");
            }
        sb.append(']');

        return sb.toString();
    }
}
