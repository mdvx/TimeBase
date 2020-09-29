package deltix.qsrv.hf.tickdb.impl;


import deltix.data.stream.MessageEncoder;
import deltix.timebase.messages.InstrumentMessage;

/**
 *
 */
class LossyMessageQueueWriter extends MessageQueueWriter <LossyMessageQueue> {

    LossyMessageQueueWriter (
        TransientStreamImpl                         stream,
        LossyMessageQueue                           queue,
        MessageEncoder<InstrumentMessage>           encoder
    )
    {
        super (stream, queue, encoder);
    }

    public void       send (InstrumentMessage msg) {
        if (prepare (msg))
            writeBuffer(msg.getTimeStampMs());
    }
}
