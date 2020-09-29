package deltix.qsrv.hf.tickdb.impl.topic;

import deltix.timebase.messages.ConstantIdentityKey;
import deltix.util.collections.generated.IntegerToObjectHashMap;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexei Osipov
 */
@ParametersAreNonnullByDefault
public class GetTopicTemporaryInstrumentMappingResponse {
    private final IntegerToObjectHashMap<ConstantIdentityKey> mapping;

    public GetTopicTemporaryInstrumentMappingResponse(IntegerToObjectHashMap<ConstantIdentityKey> mapping) {
        this.mapping = mapping;
    }

    public IntegerToObjectHashMap<ConstantIdentityKey> getMapping() {
        return mapping;
    }
}
