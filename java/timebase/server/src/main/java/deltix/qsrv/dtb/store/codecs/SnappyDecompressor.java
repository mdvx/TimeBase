package deltix.qsrv.dtb.store.codecs;

import deltix.qsrv.dtb.fs.alloc.ByteArrayHeap;
import deltix.qsrv.dtb.fs.alloc.HeapManager;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 *
 */
public class SnappyDecompressor extends BlockDecompressor {

//    public SnappyDecompressor(ByteArrayHeap heap) {
//        super(heap);
//    }

    public void                 inflate (
        byte []                     src,
        int                         srcOffset,
        int                         srcLength,
        byte []                     out,
        int                         outOffset,
        int                         outLength
    )
    {
        int infLength = 0;
        try {
            infLength = Snappy.uncompress(src, srcOffset, srcLength, out, outOffset);
        } catch (IOException e) {
            throw new deltix.util.io.UncheckedIOException(e);
        }

        if (infLength != outLength)
            throw new deltix.util.io.UncheckedIOException (
                "Inflated " + infLength + " bytes; expected: " + outLength
            );
    }
}
