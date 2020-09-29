package deltix.qsrv.hf.tickdb.impl.disruptorqueue;

import deltix.streaming.MessageChannel;
import deltix.data.stream.MessageEncoder;
import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.tickdb.impl.bytedisruptor.ByteRingBuffer;
import deltix.qsrv.hf.tickdb.pub.WriterAbortedException;
import deltix.qsrv.hf.tickdb.pub.WriterClosedException;

/**
 * @author Alexei Osipov
 */
abstract class DisruptorQueueWriter implements MessageChannel<InstrumentMessage> {
    protected final DisruptorMessageQueue queue;
    protected final ByteRingBuffer ringBuffer;
    protected final InstrumentMessageInteractiveEventWriter writer;

    private volatile boolean isOpen = true;

    DisruptorQueueWriter(DisruptorMessageQueue queue, ByteRingBuffer ringBuffer, MessageEncoder<InstrumentMessage> encoder) {
        this.queue = queue;
        this.ringBuffer = ringBuffer;
        this.writer = new InstrumentMessageInteractiveEventWriter(encoder);
    }

    @Override
    public void send(InstrumentMessage msg) {
        if (!isOpen) {
            throw new WriterClosedException(this + " is closed");
        }
        if (queue.closed) {
            throw new WriterAbortedException(queue.stream + " is closed");
        }

        if (!queue.stream.accumulateIfRequired(msg)) {
            return;
        }

        int length = writer.prepare(msg);
        writeDataToRingBuffer(length);
    }

    protected abstract void writeDataToRingBuffer(int length);

    @Override
    public void close() {
        isOpen = false;
    }

}
