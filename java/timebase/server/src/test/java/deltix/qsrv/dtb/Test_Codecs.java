package deltix.qsrv.dtb;

import deltix.qsrv.dtb.test.Codecs;
import java.io.*;
import org.junit.*;

/**
 *
 */
public class Test_Codecs {
    @Test
    public void         compressDecompress () throws IOException {
        Codecs.compressDecompress ();
    }
    
    @Test
    public void         testMessageSizeCodec () {
        Codecs.testMessageSizeCodec ();
    }
}
