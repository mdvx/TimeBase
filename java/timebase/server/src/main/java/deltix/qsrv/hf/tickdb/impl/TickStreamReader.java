package deltix.qsrv.hf.tickdb.impl;

import deltix.streaming.MessageSource;
import deltix.data.stream.RealTimeMessageSource;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.pub.query.TypedMessageSource;
import deltix.util.concurrent.IntermittentlyAvailableResource;

interface TickStreamReader extends
        MessageSource<InstrumentMessage>,
        TickStreamRelated,
        TypedMessageSource,
        IntermittentlyAvailableResource,
        RealTimeMessageSource<InstrumentMessage>
{
    void reset(long time);
}
