package deltix.qsrv.hf.pub.codec.intp;

import deltix.util.memory.MemoryDataOutput;

public interface ContainerEncoder {

    void beginWrite(MemoryDataOutput out);

    void endWrite();
}