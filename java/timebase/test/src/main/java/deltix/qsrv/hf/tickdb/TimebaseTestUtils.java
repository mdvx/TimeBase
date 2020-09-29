package deltix.qsrv.hf.tickdb;

import deltix.streaming.MessageSource;
import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.stream.MessageFileHeader;
import deltix.qsrv.hf.stream.Protocol;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.ui.tbshell.TickDBShell;
import deltix.qsrv.testsetup.DataGenerator;
import deltix.timebase.api.MarketMessageType;
import deltix.timebase.api.messages.BookUpdateAction;
import deltix.timebase.messages.IdentityKey;
import deltix.timebase.messages.InstrumentMessage;
import deltix.timebase.api.messages.L2SnapshotHelper;
import deltix.util.lang.Util;
import deltix.util.time.GMT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Description: deltix.qsrv.hf.tickdb.TimebaseTestUtils
 * Date: Mar 19, 2010
 *
 * @author Nickolay Dul
 */
public final class TimebaseTestUtils {


    public static void recreateTimebase(String location) {
        DXTickDB db = TickDBFactory.create(location);
        try {
            db.format();
        } finally {
            Util.close(db);
        }
    }

    public static void createFileStream(String timebasePath, String streamKey, String messageFilePath) throws Throwable {
        DXTickDB db = TickDBFactory.create(timebasePath);
        db.open(false);
        db.createFileStream(streamKey, messageFilePath);
        db.close();
    }

    public static void importFromFile(String timebasePath, String streamKey, int df, String messageFilePath) throws Throwable {
        DXTickDB db = TickDBFactory.create(timebasePath);
        db.open(false);

        DXTickStream first = db.createStream(streamKey, streamKey, null, df);
        MessageFileHeader header = Protocol.readHeader(new File(messageFilePath));
        first.setPolymorphic (header.getTypes());
        TickDBShell.loadMessageFile(new File(messageFilePath), first);

        db.close();
    }

    public static void populateStream(DXTickStream stream, MessageSource<InstrumentMessage> source) {
        TickLoader loader = stream.createLoader(new LoadingOptions(false));
        try {
            while (source.next()) {
                loader.send(source.getMessage());
            }
        } finally {
            Util.close(loader);
        }
    }

    public static deltix.timebase.api.messages.BestBidOfferMessage createBBO(IdentityKey instrument, long timestamp,
                                                                             double bidPrice, double bidSize, double offerPrice, double offerSize) {
        deltix.timebase.api.messages.BestBidOfferMessage bbo = new deltix.timebase.api.messages.BestBidOfferMessage();

        bbo.setSymbol(instrument.getSymbol().toString());

        bbo.setTimeStampMs(timestamp);
        bbo.setBidPrice(bidPrice);
        bbo.setBidSize(bidSize);
        bbo.setOfferPrice(offerPrice);
        bbo.setOfferSize(offerSize);

        return bbo;
    }

    public static deltix.timebase.api.messages.TradeMessage createTrade(IdentityKey instrument, long timestamp,
                                                                        double price, double size) {
        deltix.timebase.api.messages.TradeMessage trade = new deltix.timebase.api.messages.TradeMessage();

        trade.setSymbol(instrument.getSymbol().toString());

        trade.setTimeStampMs(timestamp);
        trade.setPrice(price);
        trade.setSize(size);

        return trade;
    }

    public static deltix.timebase.api.messages.BarMessage createBar(IdentityKey instrument, long timestamp,
                                                                    double open, double close, double high, double low, double volume) {
        deltix.timebase.api.messages.BarMessage bar = new deltix.timebase.api.messages.BarMessage();

        bar.setSymbol(instrument.getSymbol().toString());

        bar.setTimeStampMs(timestamp);
        bar.setOpen(open);
        bar.setClose(close);
        bar.setHigh(high);
        bar.setLow(low);
        bar.setVolume(volume);

        return bar;
    }

    public static deltix.timebase.api.messages.MarketMessage createTestMessage(Random rnd,
                                                                               MarketMessageType type,
                                                                               String symbol,
                                                                               long timestamp) {
        Class<? extends deltix.timebase.api.messages.MarketMessage> messageType;
        switch (type) {
            case TYPE_BBO:
                messageType = deltix.timebase.api.messages.BestBidOfferMessage.class;
                break;
            case TYPE_TRADE:
                messageType = deltix.timebase.api.messages.TradeMessage.class;
                break;
            case TYPE_BAR:
                messageType = deltix.timebase.api.messages.BarMessage.class;
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
        return createTestMessage(rnd, messageType, symbol, timestamp);
    }

    public static deltix.timebase.api.messages.MarketMessage createTestMessage(Random rnd,
                                                                               Class<? extends deltix.timebase.api.messages.MarketMessage> type,
                                                                               String symbol,
                                                                               long timestamp) {
        deltix.timebase.api.messages.MarketMessage message;
        if (deltix.timebase.api.messages.BestBidOfferMessage.class.isAssignableFrom(type)) {
            message = new deltix.timebase.api.messages.BestBidOfferMessage();
            deltix.timebase.api.messages.BestBidOfferMessage bbo = (deltix.timebase.api.messages.BestBidOfferMessage) message;
            switch (0) {
                case 0:
                    bbo.setBidPrice(rnd.nextDouble()*100);
                    bbo.setBidSize(rnd.nextInt(1000));
                    bbo.setOfferPrice(rnd.nextDouble()*100);
                    bbo.setOfferSize(rnd.nextInt(1000));
                    break;
                case 1:
                    bbo.setBidPrice(rnd.nextDouble()*100);
                    bbo.setBidSize(rnd.nextInt(1000));
                    break;
                default:
                    bbo.setOfferPrice(rnd.nextDouble()*100);
                    bbo.setOfferSize(rnd.nextInt(1000));
            }
        } else if (deltix.timebase.api.messages.TradeMessage.class.isAssignableFrom(type)) {
            message = new deltix.timebase.api.messages.TradeMessage();
            deltix.timebase.api.messages.TradeMessage trade = (deltix.timebase.api.messages.TradeMessage) message;
            trade.setPrice(rnd.nextDouble()*100);
            trade.setSize(rnd.nextInt(1000));
        } else if (deltix.timebase.api.messages.BarMessage.class.isAssignableFrom(type)) {
            message = new deltix.timebase.api.messages.BarMessage();
            deltix.timebase.api.messages.BarMessage bar = (deltix.timebase.api.messages.BarMessage) message;
            bar.setHigh(rnd.nextDouble()*100);
            bar.setOpen(bar.getHigh() - rnd.nextDouble()*10);
            bar.setClose(bar.getHigh() - rnd.nextDouble()*10);
            bar.setLow(Math.min(bar.getOpen(), bar.getClose()) - rnd.nextDouble()*10);
            bar.setVolume(rnd.nextInt(10000));
        } else if (deltix.timebase.api.messages.Level2Message.class.isAssignableFrom(type)) {
            message = new deltix.timebase.api.messages.Level2Message();
            deltix.timebase.api.messages.Level2Message l2m = (deltix.timebase.api.messages.Level2Message) message;
            l2m.setPrice(rnd.nextDouble()*100);
            l2m.setSize(rnd.nextInt(1000));
            l2m.setExchangeId(1 + rnd.nextInt(100));
            l2m.setDepth((short)0);
            l2m.setAction(BookUpdateAction.UPDATE);
        } else if (deltix.timebase.api.messages.L2SnapshotMessage.class.isAssignableFrom(type)) {
            message = new deltix.timebase.api.messages.L2SnapshotMessage();
            deltix.timebase.api.messages.L2SnapshotMessage snapshot = (deltix.timebase.api.messages.L2SnapshotMessage) message;
            for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++) {
                L2SnapshotHelper.setSize(snapshot, i, rnd.nextInt(1000));
                L2SnapshotHelper.setPrice(snapshot, i, rnd.nextDouble()*100); // ND TODO: Generate prices in valid order (asc or desc)
            }
        } else
            throw new IllegalStateException("Unknown message type: " + type);

        message.setTimeStampMs(timestamp);
        message.setSymbol(symbol);
        message.setCurrencyCode((short)840);
        return message;
    }

    public static void printCursor(MessageSource<InstrumentMessage> cur, PrintWriter out) {
        while (cur.next()) {
            printMessage(cur.getMessage(), out);
        }
    }

    public static void printMessage(InstrumentMessage msg, PrintWriter out) {
        out.println(messageToString(msg));
    }

    public static String messageToString(InstrumentMessage msg) {
        if (msg instanceof RawMessage) {
            return msg.toString();
        }

        StringBuilder builder = new StringBuilder(200);
        builder.append(
            String.format("%s,%s,%s,%d",
                          msg.getClass().getSimpleName(),
                    msg.getSymbol(),
                          GMT.formatDateTimeMillis(msg.getTimeStampMs()),
                    msg.getTimeStampMs())
        );

        if (msg instanceof deltix.timebase.api.messages.MarketMessage) {
            deltix.timebase.api.messages.MarketMessage mm = (deltix.timebase.api.messages.MarketMessage) msg;
            builder.append(String.format(",%s", mm.getCurrencyCode()));

            if (msg instanceof deltix.timebase.api.messages.BestBidOfferMessage) {
                deltix.timebase.api.messages.BestBidOfferMessage bbo = (deltix.timebase.api.messages.BestBidOfferMessage) msg;
                builder.append(
                  String.format(",%.2f,%.2f,%s,%.2f,%.2f,%s",
                          bbo.getBidPrice(),
                          bbo.getBidSize(),
                                ExchangeCodec.longToCode(bbo.getBidExchangeId()),
                          bbo.getOfferPrice(),
                          bbo.getOfferSize(),
                                ExchangeCodec.longToCode(bbo.getOfferExchangeId()))
                );
            } else if (msg instanceof deltix.timebase.api.messages.TradeMessage) {
                deltix.timebase.api.messages.TradeMessage trade = (deltix.timebase.api.messages.TradeMessage) msg;
                builder.append(
                        String.format(",%s,%.2f,%.2f",
                                      ExchangeCodec.longToCode(trade.getExchangeId()),
                                trade.getPrice(),
                                trade.getSize())
                );
            } else if (msg instanceof deltix.timebase.api.messages.BarMessage) {
                deltix.timebase.api.messages.BarMessage bar = (deltix.timebase.api.messages.BarMessage) msg;
                builder.append(
                        String.format(",%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                                      ExchangeCodec.longToCode(bar.getExchangeId()),
                                bar.getOpen(),
                                bar.getHigh(),
                                bar.getLow(),
                                bar.getClose(),
                                bar.getVolume())
                );
            } else if (msg instanceof deltix.timebase.api.messages.Level2Message) {
                deltix.timebase.api.messages.Level2Message l2m = (deltix.timebase.api.messages.Level2Message) msg;
                builder.append(
                        String.format(",%s,%s,%s,%.2f,%.2f",
                                l2m.isAsk(),
                                l2m.getAction(),
                                      ExchangeCodec.longToCode(l2m.getExchangeId()),
                                l2m.getPrice(),
                                l2m.getSize())
                );
            } else if (msg instanceof deltix.timebase.api.messages.L2SnapshotMessage) {
                deltix.timebase.api.messages.L2SnapshotMessage snapshot = (deltix.timebase.api.messages.L2SnapshotMessage) msg;
                builder.append(
                        String.format(",%s,%s", snapshot.isAsk(), ExchangeCodec.longToCode(snapshot.getExchangeId()))
                );
                for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++)
                    builder.append(String.format(",%.2f,%.2f", L2SnapshotHelper.getPrice(snapshot, i), L2SnapshotHelper.getSize(snapshot, i)));
            }
        }
        return builder.toString();
    }

    ////////////////////////////// HELPER CLASSES ///////////////////////

    public static class IterableMessageSource<T> implements MessageSource<T> {
        private final Iterable<? extends T> source;
        private Iterator<? extends T> iterator;

        @SafeVarargs
        @SuppressWarnings("varargs")
        public IterableMessageSource(T... messages) {
            this(new ArrayList<T>(Arrays.asList(messages)));
        }

        public IterableMessageSource(Iterable<? extends T> source) {
            this.source = source;
            reset();
        }

        public void reset() {
            iterator = source.iterator();
        }

        @Override
        public T getMessage() {
            return iterator.next();
        }

        @Override
        public boolean next() {
            return iterator.hasNext();
        }

        @Override
        public boolean isAtEnd() {
            return !iterator.hasNext();
        }

        @Override
        public void close() {
        }
    }

    @SuppressFBWarnings(value = "PREDICTABLE_RANDOM", justification = "Random is used for fuzzy tests, not cryptography")
    public static class CycleMarketMessageSource implements MessageSource<InstrumentMessage> {
        private final int maxCycleCount;
        private final long start;
        private final long interval;

        private final MarketMessageType[] types;
        private final String[] symbols;

        private final Random rnd = new Random();

        private InstrumentMessage message;
        private int counter;

        public CycleMarketMessageSource(int maxCycleCount, long start, long interval,
                                        MarketMessageType[] types, String[] symbols) {
            this.maxCycleCount = maxCycleCount;
            this.start = start;
            this.interval = interval;
            this.types = types;
            this.symbols = symbols;
        }

        public void reset() {
            message = null;
            counter = 0;
        }

        @Override
        public InstrumentMessage getMessage() {
            return message;
        }

        @Override
        public boolean next() {
            int cycleCount = counter / (types.length * symbols.length);
            if (cycleCount > maxCycleCount)
                return false;

            int cycleIndex = counter % (types.length * symbols.length);
            int typeIndex = cycleIndex / (symbols.length);
            int entityTypeIndex = (cycleIndex % (symbols.length)) / symbols.length;
            int symbolIndex = (cycleIndex % (symbols.length)) % symbols.length;

            long time = start + counter * interval;
            message = createTestMessage(rnd, types[typeIndex], symbols[symbolIndex], time);

            counter++;
            return true;
        }

        @Override
        public boolean isAtEnd() {
            return false;
        }

        @Override
        public void close() {
        }
    }

    @SuppressFBWarnings(value = "PREDICTABLE_RANDOM", justification = "Random is used for internal tests, not cryptography")
    public static class DataGeneratorEx extends DataGenerator {

        protected deltix.timebase.api.messages.Level2Message level2 = new deltix.timebase.api.messages.Level2Message();
        protected deltix.timebase.api.messages.L2SnapshotMessage l2snapshot = new deltix.timebase.api.messages.L2SnapshotMessage();

        private static final Random RANDOM = new Random(System.currentTimeMillis());

        public DataGeneratorEx(GregorianCalendar calendar, int interval, String... symbols) {
            super(calendar, interval, symbols);

            level2.setPrice(nextDouble());
            level2.setSize(1000);
            level2.setCurrencyCode((short)840);

            l2snapshot.setCurrencyCode((short)840);

        }

        @Override
        public boolean next() {
            generateBBOTradeL2AndL2Snapshot();
            return true;
        }

        private void generateBBOTradeL2AndL2Snapshot() {
            InstrumentMessage message;

            if (count % 13 == 0) {
                message = level2;
                ((deltix.timebase.api.messages.Level2Message) message).setSize(nextDouble());
                ((deltix.timebase.api.messages.Level2Message) message).setPrice(nextDouble());
                ((deltix.timebase.api.messages.Level2Message) message).setAction(RANDOM.nextInt(3) % 2 == 0 ? BookUpdateAction.INSERT : BookUpdateAction.UPDATE);

            } else if (count % 10 == 0) {
                message = trade;
                ((deltix.timebase.api.messages.TradeMessage) message).setSize(nextDouble());
                ((deltix.timebase.api.messages.TradeMessage) message).setPrice(nextDouble());
            } else if (count % 7 == 0) {
                message = l2snapshot;
                l2snapshot.setIsAsk(RANDOM.nextInt(2) != 0);
                for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++) {
                    L2SnapshotHelper.setSize(((deltix.timebase.api.messages.L2SnapshotMessage) message), i, RANDOM.nextInt(1000));
                    L2SnapshotHelper.setPrice(((deltix.timebase.api.messages.L2SnapshotMessage) message), i, RANDOM.nextDouble() * 100);
                }
            } else if (count % 2 == 0) {
                message = bbo1;
                ((deltix.timebase.api.messages.BestBidOfferMessage) message).setBidSize(nextDouble());
                ((deltix.timebase.api.messages.BestBidOfferMessage) message).setBidPrice(nextDouble());
            } else {
                message = bbo2;
                ((deltix.timebase.api.messages.BestBidOfferMessage) message).setOfferSize(nextDouble());
                ((deltix.timebase.api.messages.BestBidOfferMessage) message).setOfferPrice(nextDouble());

            }

            message.setSymbol(symbols.next());
            message.setTimeStampMs(symbols.isReseted() ? getNextTime() : getActualTime());

            current = message;
            count++;

        }

        private void generateL2andL2Shapshot() {
            InstrumentMessage message;

            if (count % 3 == 0) {
                message = l2snapshot;
                ((deltix.timebase.api.messages.L2SnapshotMessage) message).setIsAsk(false);
                for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++) {
                    L2SnapshotHelper.setSize(((deltix.timebase.api.messages.L2SnapshotMessage) message), i, RANDOM.nextInt(1000));
                    L2SnapshotHelper.setPrice(((deltix.timebase.api.messages.L2SnapshotMessage) message), i, RANDOM.nextDouble() * 100);
                }
            } else if (count % 2 == 0) {
                message = l2snapshot;
                ((deltix.timebase.api.messages.L2SnapshotMessage) message).setIsAsk(true);
                for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++) {
                    L2SnapshotHelper.setSize(((deltix.timebase.api.messages.L2SnapshotMessage) message), i, RANDOM.nextInt(1000));
                    L2SnapshotHelper.setPrice(((deltix.timebase.api.messages.L2SnapshotMessage) message), i, RANDOM.nextDouble() * 100);
                }

            } else {
                message = level2;
                ((deltix.timebase.api.messages.Level2Message) message).setSize(nextDouble());
                ((deltix.timebase.api.messages.Level2Message) message).setPrice(nextDouble());
                ((deltix.timebase.api.messages.Level2Message) message).setAction(RANDOM.nextInt(3) % 2 == 0 ? BookUpdateAction.INSERT : BookUpdateAction.UPDATE);
            }

            message.setSymbol(symbols.next());
            message.setTimeStampMs(symbols.isReseted() ? getNextTime() : getActualTime());

            current = message;
            count++;

        }
    }

}
