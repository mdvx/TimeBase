package deltix.qsrv.hf.tickdb.tool;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.timebase.messages.InstrumentKey;
import deltix.util.collections.generated.ObjectArrayList;

public class S3SpaceMetadata {

    public static final long DEFAULT_START_TIME = Long.MIN_VALUE;

    /**
     * Version of S3 utilities set.
     */
    public String version = "0.1";

    /**
     * First replicated message timestamp.
     */
    public long startTime = DEFAULT_START_TIME;

    /**
     * List of instrument identities stored in current space.
     */
    public ObjectArrayList<InstrumentKey> symbols = new ObjectArrayList<>();

    public static S3SpaceMetadata fromStream(DXTickStream stream, String space, long firstTimestamp) {
        S3SpaceMetadata metadata = new S3SpaceMetadata();
        for (IdentityKey IdentityKey : stream.listEntities(space)) {
            metadata.symbols.add(new InstrumentKey(IdentityKey));
        }
        metadata.version = "0.1";
        metadata.startTime = firstTimestamp;
        return metadata;
    }

}
