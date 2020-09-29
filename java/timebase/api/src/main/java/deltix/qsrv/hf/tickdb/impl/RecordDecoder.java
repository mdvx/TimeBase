package deltix.qsrv.hf.tickdb.impl;

import deltix.data.stream.MessageDecoder;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;

/**
 *
 */
public interface RecordDecoder<T extends InstrumentMessage> extends MessageDecoder<T> {

    public RecordClassDescriptor    getCurrentType();

    public int                      getCurrentTypeIndex();

    // TODO: add for performance
    //public int                      getCurrentEntityIndex();
}
