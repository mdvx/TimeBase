package deltix.qsrv.hf.tickdb;

import deltix.qsrv.hf.pub.CurrencyCodec;
import deltix.qsrv.hf.pub.ExchangeCodec;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.tickdb.pub.StreamOptions;
import deltix.qsrv.hf.tickdb.pub.StreamScope;
import deltix.qsrv.hf.tickdb.pub.TickCursor;
import deltix.timebase.api.messages.MarketMessage;
import deltix.util.currency.CurrencyCodeList;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * User: BazylevD
 * Date: Apr 16, 2009
 * Time: 4:22:32 PM
 */
public class TickDBUtil {
    public static String USER_HOME = System.getProperty("user.home") + "\\";

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    Callback callback;
    private final StringBuilder sb = new StringBuilder();

    interface Callback {
        void callback(InstrumentMessage msg, TickCursor cursor);
    }

    public TickDBUtil() {
        callback = null;
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String           toString(TickCursor cursor) {
        final StringBuilder sb = new StringBuilder();
        printCursor(cursor, sb);
        return sb.toString ();
    }

    private void printCursor(TickCursor cur, StringBuilder out) {
        if (cur == null) {
            System.out.println("NO DATA");
        } else {
            while (cur.next()) {
                InstrumentMessage msg = cur.getMessage();
                if (callback != null)
                    callback.callback(msg, cur);

                print(msg, out);
            }
        }
    }

    public void print(InstrumentMessage msg, StringBuilder out) {

        if (out.length() > 0)
            out.append(System.lineSeparator());

        if (msg instanceof RawMessage) {
            out.append(msg.toString());
            return;
        }

        out.append(String.format(
                "%s,%s,%s,%d",
                msg.getClass().getSimpleName(),
                msg.getSymbol(),
                df.format(msg.getTimeStampMs()),
                msg.getTimeStampMs()
        ));

        if (msg instanceof deltix.timebase.api.messages.MarketMessage) {
            deltix.timebase.api.messages.MarketMessage mm = (deltix.timebase.api.messages.MarketMessage) msg;
            appendCurrency(mm, out); //TODO: rebuild

            if (msg instanceof deltix.timebase.api.messages.BestBidOfferMessage) {
                deltix.timebase.api.messages.BestBidOfferMessage bbo = (deltix.timebase.api.messages.BestBidOfferMessage) msg;

                out.append(String.format(
                    ",%.2f,%.2f,%s,%.2f,%.2f,%s",
                        bbo.getBidPrice(),
                        bbo.getBidSize(),
                    ExchangeCodec.longToCode(bbo.getBidExchangeId()),
                        bbo.getOfferPrice(),
                        bbo.getOfferSize(),
                    ExchangeCodec.longToCode(bbo.getOfferExchangeId())
                ));
            } else if (msg instanceof deltix.timebase.api.messages.TradeMessage) {
                deltix.timebase.api.messages.TradeMessage trade = (deltix.timebase.api.messages.TradeMessage) msg;

                out.append(String.format(
                        ",%s,%.2f,%.2f",
                        ExchangeCodec.longToCode(trade.getExchangeId()),
                        trade.getPrice(),
                        trade.getSize()
                ));
            } else if (msg instanceof deltix.timebase.api.messages.BarMessage) {
                deltix.timebase.api.messages.BarMessage bar = (deltix.timebase.api.messages.BarMessage) msg;

                out.append(String.format(
                        ",%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                        ExchangeCodec.longToCode(bar.getExchangeId()),
                        bar.getOpen(),
                        bar.getHigh(),
                        bar.getLow(),
                        bar.getClose(),
                        bar.getVolume()
                ));
            }
        }

    }



    // the method is not thread safe, because it uses sb instance 
    public String toString(InstrumentMessage msg) {
        if (msg instanceof RawMessage) {
            return msg.toString();
        }

        sb.setLength(0);
        sb.append(
        String.format(
                "%s,%s,%s,%d",
                msg.getClass().getSimpleName(),
                msg.getSymbol(),
                df.format(msg.getTimeStampMs()),
                msg.getTimeStampMs()
        ));

        if (msg instanceof deltix.timebase.api.messages.MarketMessage) {
            deltix.timebase.api.messages.MarketMessage mm = (deltix.timebase.api.messages.MarketMessage) msg;

            appendCurrency(mm, sb);

            if (msg instanceof deltix.timebase.api.messages.BestBidOfferMessage) {
                deltix.timebase.api.messages.BestBidOfferMessage bbo = (deltix.timebase.api.messages.BestBidOfferMessage) msg;

                sb.append(
                String.format(
                    ",%.2f,%.2f,%s,%.2f,%.2f,%s",
                        bbo.getBidPrice(),
                        bbo.getBidSize(),
                        ExchangeCodec.longToCode(bbo.getBidExchangeId()),
                        bbo.getOfferPrice(),
                        bbo.getOfferSize(),
                        ExchangeCodec.longToCode(bbo.getOfferExchangeId())
                ));
            } else if (msg instanceof deltix.timebase.api.messages.TradeMessage) {
                deltix.timebase.api.messages.TradeMessage trade = (deltix.timebase.api.messages.TradeMessage) msg;

                sb.append(
                String.format(
                        ",%s,%.2f,%.2f",
                        ExchangeCodec.longToCode(trade.getExchangeId()),
                        trade.getPrice(),
                        trade.getSize()
                ));
            } else if (msg instanceof deltix.timebase.api.messages.BarMessage) {
                deltix.timebase.api.messages.BarMessage bar = (deltix.timebase.api.messages.BarMessage) msg;

                sb.append(
                String.format(
                        ",%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                        ExchangeCodec.longToCode(bar.getExchangeId()),
                        bar.getOpen(),
                        bar.getHigh(),
                        bar.getLow(),
                        bar.getClose(),
                        bar.getVolume()
                ));
            }
        }

        return sb.toString();
    }

    public void appendCurrency(MarketMessage msg, StringBuilder sb) {
        short code = msg.getCurrencyCode();
        CurrencyCodeList.CurrencyInfo info = CurrencyCodeList.getInfoByNumeric(code);
        // but it may contain CurrencyCodec encoded value
        if (info == null)
            info = CurrencyCodec.getCurrencyCode(code);

        sb.append(
                String.format(
                        ",%s",
                        info != null ? info.numericCode : code
                ));
    }

    public static StreamOptions transientTradeStreamOptions () {
        StreamOptions   options = new StreamOptions (StreamScope.TRANSIENT, null, null, StreamOptions.MAX_DISTRIBUTION);

        options.setFixedType (StreamConfigurationHelper.mkUniversalTradeMessageDescriptor ());

        return (options);
    }
}
