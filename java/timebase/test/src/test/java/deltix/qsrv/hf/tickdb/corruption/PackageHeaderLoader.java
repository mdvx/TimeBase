package deltix.qsrv.hf.tickdb.corruption;

import deltix.dfp.Decimal64Utils;
import deltix.gflog.Log;
import deltix.gflog.LogFactory;
import deltix.qsrv.hf.pub.ExchangeCodec;

import deltix.qsrv.hf.tickdb.pub.TickLoader;
import deltix.timebase.messages.*;
import deltix.timebase.api.messages.*;
import deltix.timebase.api.messages.universal.*;
import deltix.util.collections.generated.ObjectArrayList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PackageHeaderLoader implements MessageLoader {

    private static final Log LOGGER = LogFactory.getLog(PackageHeaderLoader.class);

    private final TickLoader loader;
    private final long exchangeCode;
    private final long snapshotInterval;

    private final PackageHeader snapshot = new PackageHeader();
    private final PackageHeader update = new PackageHeader();

    private final Random random = new Random();

    private long currentTime;
    private long lastSnapshotTimeMillis = 0;

    static CompositeLoader createComposite(TickLoader loader, String exchange, String... symbols) {
        List<MessageLoader> loaders = new ArrayList<>();
        for (int i = 0; i < symbols.length; ++i) {
            loaders.add(new PackageHeaderLoader(loader, symbols[i], exchange));
        }

        return new CompositeLoader(loaders);
    }

    PackageHeaderLoader(TickLoader loader,
                        String symbol, String exchange)
    {
        this(loader, 10000, symbol, exchange, 50, 1000d);
    }

    PackageHeaderLoader(TickLoader loader,
                        long snapshotInterval,
                        String symbol, String exchange,
                        int levels, double midPrice)
    {
        this.loader = loader;
        this.snapshotInterval = snapshotInterval;
        this.exchangeCode = ExchangeCodec.codeToLong(exchange);

        snapshot.setSymbol(symbol);
        snapshot.setPackageType(PackageType.PERIODICAL_SNAPSHOT);
        snapshot.setEntries(createSnapshotEntries(levels, midPrice));
        
        update.setSymbol(symbol);
        update.setPackageType(PackageType.INCREMENTAL_UPDATE);
        update.setEntries(createUpdateEntries());
    }

    private ObjectArrayList<BaseEntryInfo> createSnapshotEntries(int levels, double midPrice) {
        ObjectArrayList<BaseEntryInfo> entries = new ObjectArrayList<>();

        double size = 1000;
        double bidPrice = midPrice - 1;
        for (int i = 0; i < levels; ++i) {
            L2EntryNew entry = new L2EntryNew();
            entry.setLevel((short) i);
            entry.setExchangeId(exchangeCode);
            entry.setSide(QuoteSide.BID);
            entry.setPrice(Decimal64Utils.fromDouble(bidPrice));
            entry.setSize(Decimal64Utils.fromDouble(size));

            entries.add(entry);
            bidPrice -= random.nextBoolean() ? 0.01 : 0.02;
            size += random.nextBoolean() ? 1 : -1;
        }

        size = 1000;
        double askPrice = midPrice + 1;
        for (int i = 0; i < levels; ++i) {
            L2EntryNew entry = new L2EntryNew();
            entry.setLevel((short) i);
            entry.setExchangeId(exchangeCode);
            entry.setSide(QuoteSide.ASK);
            entry.setPrice(Decimal64Utils.fromDouble(askPrice));
            entry.setSize(Decimal64Utils.fromDouble(size));

            entries.add(entry);
            askPrice += random.nextBoolean() ? 0.01 : 0.02;
            size += random.nextBoolean() ? 1 : -1;
        }

        return entries;
    }

    private ObjectArrayList<BaseEntryInfo> createUpdateEntries() {
        ObjectArrayList<BaseEntryInfo> entries = new ObjectArrayList<>();
        for (int i = 0; i < 3; ++i) {
            L2EntryUpdate entry = new L2EntryUpdate();
            entry.setSide(QuoteSide.BID);
            entry.setAction(BookUpdateAction.UPDATE);
            entry.setExchangeId(exchangeCode);

            entries.add(entry);
        }

        for (int i = 0; i < 3; ++i) {
            L2EntryUpdate entry = new L2EntryUpdate();
            entry.setSide(QuoteSide.ASK);
            entry.setAction(BookUpdateAction.UPDATE);
            entry.setExchangeId(exchangeCode);

            entries.add(entry);
        }

        return entries;
    }

    @Override
    public void update(long timestamp) {
        currentTime = timestamp;
        snapshot.setTimeStampMs(timestamp);
        update.setTimeStampMs(timestamp);
        int levels = snapshot.getEntries().size();
        int level = random.nextInt(levels);
        ObjectArrayList<BaseEntryInfo> entries = update.getEntries();
        for (int i = 0; i < entries.size(); ++i) {
            level = (level + 1) % levels;

            L2EntryUpdate entry = (L2EntryUpdate) entries.get(i);
            L2EntryNew snapshotEntry = (L2EntryNew) snapshot.getEntries().get(level);
            entry.setPrice(snapshotEntry.getPrice());
            entry.setSize(Decimal64Utils.fromDouble(
                Decimal64Utils.toDouble(snapshotEntry.getSize()) + (random.nextBoolean() ? 10 : -10)
                )
            );
            entry.setLevel(snapshotEntry.getLevel());
        }
    }

    @Override
    public long send() {
        long messages = 0;
        if (currentTime - lastSnapshotTimeMillis > snapshotInterval) {
            loader.send(snapshot);
            messages++;
            lastSnapshotTimeMillis = currentTime;
        }

        loader.send(update);
        messages++;

        return messages;
    }

    @Override
    public void close() {
        loader.close();
        System.out.println(LocalDateTime.now() + ": " + "Loader closed");
    }

}
