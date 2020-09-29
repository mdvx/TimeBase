package deltix.data.stream;

import deltix.util.lang.Filter;
import deltix.util.memory.MemoryDataInput;

/**
 *
 */
public interface MessageDecoder <T> {
    public T                    decode (
        Filter <? super T>          filter,
        MemoryDataInput             in
    );
}
