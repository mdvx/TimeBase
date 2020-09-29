package deltix.qsrv.hf.pub.codec;

import deltix.util.memory.MemoryDataInput;

public interface CharSequenceDecoder {

    CharSequence readCharSequence(MemoryDataInput in);

}
