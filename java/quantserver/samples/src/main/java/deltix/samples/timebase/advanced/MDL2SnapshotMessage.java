package deltix.samples.timebase.advanced;

import deltix.gflog.AppendableEntry;
import deltix.gflog.LogEntryBuilder;
import deltix.gflog.Loggable;
import deltix.timebase.api.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.RecordInfo;
import deltix.util.collections.generated.ObjectArrayList;
import deltix.util.lang.Util;

public class MDL2SnapshotMessage extends InstrumentMessage implements Loggable {
    public static final int DEFAULT_DEPTH = 10;

    /** This field is used for measuring internal feed latency. Some feed provides can initialize this into Java nano-time. */
    public transient long nanoTime;

    public CharSequence clientId;

    public CharSequence requestId;

    public long sequenceId;

    public ObjectArrayList<MDEntry> entries;

    public MDL2SnapshotMessage() {
        this(null, DEFAULT_DEPTH);
    }

    public MDL2SnapshotMessage(CharSequence requestId, int orderBookDepth) {
        this.requestId = requestId;
        this.entries = new ObjectArrayList<>(orderBookDepth * 2);
    }

    public void setEntry(int index, MDEntry entry) {
        entries.set(index, entry);
    }

    public MDEntry getEntry(int index) {
        return entries.get(index);
    }

    public void addEntry(MDEntry entry) {
        entries.add(entry);
    }

    public void clearEntries() {
        entries.clear();
    }

    @Override
    public MDL2SnapshotMessage copyFrom(RecordInfo template) {
        super.copyFrom(template);
        if (template instanceof MDL2SnapshotMessage) {
            MDL2SnapshotMessage msg = (MDL2SnapshotMessage) template;

            this.clientId = Util.toNullableString(msg.clientId);
            this.requestId = Util.toNullableString(msg.requestId);
            this.sequenceId = msg.sequenceId;
            this.entries = null;

            if (msg.entries != null && !msg.entries.isEmpty()) {
                this.entries = new ObjectArrayList<>(msg.entries.size());
                for (int i = 0; i < msg.entries.size(); i++) {
                    MDEntry entry = msg.entries.get(i);
                    this.entries.add(entry != null ? entry.copy(true) : null);
                }
            }
        }
        return this;
    }

    @Override
    protected MDL2SnapshotMessage createInstance() {
        return new MDL2SnapshotMessage();
    }

    @Override
    public MDL2SnapshotMessage clone() {
        MDL2SnapshotMessage result = createInstance();
        result.copyFrom(this);
        return result;
    }

    @Override
    public void appendTo(AppendableEntry entry) {
        entry.append("MDL2SnapshotMessage, ").
                append(getSymbol()).append(':').append(getInstrumentType()).
                append(", ").append(requestId).
                append(", ").append(sequenceId);

        if (entries != null && !entries.isEmpty()) {
            for (int i = 0; i < entries.size(); i++) {
                MDEntry mdentry = entries.get(i);
                entry.append("\n\t").append(mdentry);
            }
        }
    }

    @Override
    public String toString() {
        LogEntryBuilder builder = new LogEntryBuilder(512);
        appendTo(builder);
        return builder.toString();
    }
}
