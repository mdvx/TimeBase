package deltix.qsrv.dtb.store.codecs;

import deltix.util.collections.generated.*;

/**
 *
 */
public abstract class BlockCompressor {

    private final ByteArrayList       buffer;

    public BlockCompressor(ByteArrayList buffer) {
        this.buffer = buffer;
    }

    public abstract byte             code();

    public ByteArrayList        getReusableBuffer() {
        return buffer;
    }

    public abstract int                  deflate (
        byte []                     src,
        int                         offset,
        int                         length,
        ByteArrayList               appendTo
    );
}