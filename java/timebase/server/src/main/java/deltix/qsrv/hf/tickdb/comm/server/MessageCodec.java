package deltix.qsrv.hf.tickdb.comm.server;

import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.impl.RecordDecoder;
import deltix.qsrv.hf.tickdb.pub.CommonOptions;
import deltix.timebase.messages.InstrumentMessage;

public abstract class MessageCodec {

    public static RecordDecoder<InstrumentMessage> createDecoder(RecordClassDescriptor[] types, CommonOptions options) {
        if (options.raw)
            return new SimpleRawDecoder(types);

        boolean compiled = options.channelQOS == ChannelQualityOfService.MAX_THROUGHPUT;

        return new PolyBoundDecoder(options.getTypeLoader(), CodecFactory.get(compiled), types);
    }
}
