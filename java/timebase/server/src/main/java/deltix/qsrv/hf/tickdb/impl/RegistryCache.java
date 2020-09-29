package deltix.qsrv.hf.tickdb.impl;

import deltix.qsrv.dtb.store.pub.SymbolRegistry;
import deltix.timebase.messages.ConstantIdentityKey;
import deltix.timebase.messages.IdentityKey;
import deltix.timebase.messages.InstrumentMessage;
import deltix.util.collections.CharSequenceToIntegerMap;
import deltix.util.collections.generated.IntegerToObjectHashMap;
import org.apache.commons.lang.mutable.MutableBoolean;

public class RegistryCache {
    private final SymbolRegistry registry;

    private final CharSequenceToIntegerMap symbolToIndex = new CharSequenceToIntegerMap();
    private final IntegerToObjectHashMap<ConstantIdentityKey> indexToSymbol = new IntegerToObjectHashMap<>();

    public RegistryCache(SymbolRegistry registry) {
        this.registry = registry;
    }

    /*
        Returns index of instrument message symbol
     */
    public synchronized int             encode(InstrumentMessage msg, MutableBoolean exists) {
        int index = symbolToIndex.get(msg.getSymbol(), SymbolRegistry.NO_SUCH_SYMBOL);

        if (index == SymbolRegistry.NO_SUCH_SYMBOL) {
            // TODO: Investigate if .intern() may have any negative effect
            index = registry.registerSymbol(msg.getSymbol().toString().intern(), null);

            exists.setValue(false);
            symbolToIndex.put(msg.getSymbol(), index);
        } else {
            exists.setValue(true);
        }

        return index;
    }

    synchronized IdentityKey     get(int index) {
        ConstantIdentityKey key = indexToSymbol.get(index, null);

        if (key == null) {
            synchronized (registry) {
                String symbol = registry.idToSymbol(index);

                if (symbol != null) {
                    //String data = registry.getEntityData(index);
                    key = new ConstantIdentityKey(symbol);
                }

                indexToSymbol.put(index, key);
            }
        }

        return key;
    }

    /*
        Assign message & type based in symbols index
     */
    public synchronized void             decode(InstrumentMessage msg, int index) {
        ConstantIdentityKey key = indexToSymbol.get(index, null);

        if (key == null) {
            key = addMissingIndex(index);
        }

        msg.setSymbol(key.symbol);
    }

    private ConstantIdentityKey addMissingIndex(int index) {
        ConstantIdentityKey key;
        synchronized (registry) {
            String symbol = registry.idToSymbol(index);
            
            assert symbol != null;

            // Note: This enum lookup is relatively costly.
            // TODO: Consider storing "InstrumentType" in the registry.
            String data = registry.getEntityData(index);

            key = new ConstantIdentityKey(symbol);
            indexToSymbol.put(index, key);
        }
        return key;
    }
}
