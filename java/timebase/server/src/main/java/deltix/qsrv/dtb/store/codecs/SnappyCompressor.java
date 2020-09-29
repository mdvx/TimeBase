package deltix.qsrv.dtb.store.codecs;

import deltix.util.collections.generated.ByteArrayList;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 *
 */
public class SnappyCompressor extends BlockCompressor {

    public SnappyCompressor(ByteArrayList buffer) {
        super(buffer);
    }

    @Override
    public byte                 code() {
        return BlockCompressorFactory.getCode(Algorithm.SNAPPY);
    }

    @Override
    public int deflate(byte[] src, int offset, int length, ByteArrayList appendTo) {
        int maxCompressedLength = Snappy.maxCompressedLength(length);
        int size = appendTo.size();
        appendTo.ensureCapacity(size + maxCompressedLength);
        byte[] data = appendTo.getInternalBuffer();
        int compressedLength = 0;
        try {
            compressedLength = Snappy.compress(src, offset, length, data, size);
        } catch (IOException e) {
            throw new deltix.util.io.UncheckedIOException(e);
        }

        appendTo.setSize(size + compressedLength);

        return compressedLength;
    }
}
