package deltix.samples.timebase.advanced;

import deltix.gflog.AppendableEntry;
import deltix.gflog.LogEntryBuilder;
import deltix.gflog.Loggable;
import deltix.util.lang.Util;

public class MDEntry implements Loggable {

    public double size;

    public double price;

    // Alphanumeric
    public long currencyCode;

    public CharSequence quoteId;

    public CharSequence originator;

    public MDQuoteCondition quoteCondition;

    public int positionNo;

    public MDEntryType entryType;

    public MDEntry copy(boolean deep) {
        MDEntry entry = new MDEntry();
        entry.copy(this, deep);
        return entry;
    }

    public void copy(MDEntry entry, boolean deep) {
        this.size = entry.size;
        this.price = entry.price;
        this.currencyCode = entry.currencyCode;
        this.quoteId = copy(deep, entry.quoteId);
        this.originator = copy(deep, entry.originator);
        this.quoteCondition = entry.quoteCondition;
        this.positionNo = entry.positionNo;
        this.entryType = entry.entryType;
    }

    protected CharSequence copy(boolean deep, CharSequence value) {
        return deep ? Util.toNullableString(value) : value;
    }

    @Override
    public void appendTo(AppendableEntry entry) {
        entry.append("MDEntry, ").
                append(quoteId).
                append(", ").append(price).
                append(", ").append(size).
                append(", ").append(entryType).
                append(", ").append(quoteCondition).
                append(", ").appendAlphanumeric(currencyCode).
                append(", ").append(positionNo).
                append(", ").append(originator);
    }

    @Override
    public String toString() {
        LogEntryBuilder builder = new LogEntryBuilder(64);
        appendTo(builder);
        return builder.toString();
    }
}
