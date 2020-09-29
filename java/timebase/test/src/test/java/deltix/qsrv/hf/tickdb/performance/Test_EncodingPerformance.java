package deltix.qsrv.hf.tickdb.performance;


import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.codec.BoundDecoder;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedBoundEncoder;
import deltix.qsrv.hf.pub.md.FloatDataType;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.StreamConfigurationHelper;
import deltix.timebase.messages.InstrumentMessage;
import deltix.timebase.api.messages.BookUpdateAction;
import deltix.timebase.api.messages.L2SnapshotHelper;
import deltix.util.collections.generated.ObjectArrayList;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;
import org.junit.Test;

import java.util.Random;

public class Test_EncodingPerformance {

    private static final Random RANDOM = new Random(2015);
    private final int MESSAGES = 10_000_000;

    @Test
    public void testL2Message() {

        System.out.println("L2Message ...");
        deltix.timebase.api.messages.L2Message message = new deltix.timebase.api.messages.L2Message();

        message.setActions(new ObjectArrayList<>(10));

        for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++) {
            deltix.timebase.api.messages.Level2Action action = new deltix.timebase.api.messages.Level2Action();
            action.setAction(BookUpdateAction.INSERT);
            action.setSize(RANDOM.nextInt(1000));
            action.setPrice(RANDOM.nextDouble() * 100);
            action.setIsAsk(RANDOM.nextInt(1) > 0);
            action.setLevel((short)0);

            message.getActions().add(action);
        }

        CodecFactory factory = CodecFactory.newCompiledCachingFactory();

        RecordClassDescriptor mmDescriptor = StreamConfigurationHelper.mkMarketMessageDescriptor(null);

        RecordClassDescriptor actionDescriptor =
                StreamConfigurationHelper.mkLevel2ActionDescriptor(
                        FloatDataType.ENCODING_FIXED_DOUBLE,
                        FloatDataType.ENCODING_FIXED_DOUBLE
                );


        RecordClassDescriptor descriptor =
                StreamConfigurationHelper.mkLevel2IncrementMessageDescriptor(mmDescriptor,
                        null,
                        null,
                        actionDescriptor);

        System.out.println("Test ENCODING_FIXED_DOUBLE ...");

        test(factory, descriptor, message);

        System.out.println("Test ENCODING_SCALE_AUTO ...");
        actionDescriptor =
                StreamConfigurationHelper.mkLevel2ActionDescriptor(
                        FloatDataType.ENCODING_SCALE_AUTO,
                        FloatDataType.ENCODING_SCALE_AUTO
                );

        descriptor = StreamConfigurationHelper.mkLevel2IncrementMessageDescriptor(mmDescriptor,
                        null,
                        null,
                        actionDescriptor);

        test(factory, descriptor, message);

        System.out.println("Test ENCODING_DECIMAL64 ...");
        actionDescriptor =
                StreamConfigurationHelper.mkLevel2ActionDescriptor(
                        FloatDataType.ENCODING_DECIMAL64,
                        FloatDataType.ENCODING_DECIMAL64
                );

        descriptor = StreamConfigurationHelper.mkLevel2IncrementMessageDescriptor(mmDescriptor,
                null,
                null,
                actionDescriptor);

        test(factory, descriptor, message);
    }

    @Test
    public void testBarMessage() {

        deltix.timebase.api.messages.BarMessage message = new deltix.timebase.api.messages.BarMessage();

        message.setClose(RANDOM.nextInt(10000) / 100.0);
        message.setOpen(message.getClose() + RANDOM.nextInt(1000) / 100.0);
        message.setHigh(message.getOpen() + RANDOM.nextInt(1000) / 100.0);
        message.setLow(message.getOpen() - RANDOM.nextInt(1000) / 100.0);
        message.setVolume(RANDOM.nextInt(1000));

        CodecFactory factory = CodecFactory.newCompiledCachingFactory();

        System.out.println("Test ENCODING_FIXED_DOUBLE ...");
        RecordClassDescriptor descriptor = StreamConfigurationHelper.mkBarMessageDescriptor(
                null, "999", 999, FloatDataType.ENCODING_FIXED_DOUBLE, FloatDataType.ENCODING_FIXED_DOUBLE);
        test(factory, descriptor, message);

        descriptor = StreamConfigurationHelper.mkBarMessageDescriptor(
                null, "999", 999, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO);

        System.out.println("Test ENCODING_SCALE_AUTO ...");
        test(factory, descriptor, message);
    }

    @Test
    public void testL2SnapshotMessage() {

        deltix.timebase.api.messages.L2SnapshotMessage message = new deltix.timebase.api.messages.L2SnapshotMessage();

        message.setIsAsk(RANDOM.nextInt(2) != 0);
        for (int i = 0; i < deltix.timebase.api.messages.L2SnapshotMessage.MAX_DEPTH; i++) {
            L2SnapshotHelper.setSize(message, i, RANDOM.nextInt(1000));
            L2SnapshotHelper.setPrice(message, i, RANDOM.nextDouble() * 100);
        }

        CodecFactory factory = CodecFactory.newCompiledCachingFactory();

        System.out.println("Test ENCODING_FIXED_DOUBLE ...");
        RecordClassDescriptor descriptor = StreamConfigurationHelper.mkL2SnapshotMessageDescriptor
                (null, null, null, FloatDataType.ENCODING_FIXED_DOUBLE, FloatDataType.ENCODING_FIXED_DOUBLE);
        test(factory, descriptor, message);

        System.out.println("Test ENCODING_SCALE_AUTO ...");
        descriptor = StreamConfigurationHelper.mkL2SnapshotMessageDescriptor
                (null, null, null, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO);

        test(factory, descriptor, message);

        System.out.println("Test ENCODING_DECIMAL64 ...");
        descriptor = StreamConfigurationHelper.mkL2SnapshotMessageDescriptor
                (null, null, null, FloatDataType.ENCODING_DECIMAL64, FloatDataType.ENCODING_DECIMAL64);

        test(factory, descriptor, message);
    }

    public void test(CodecFactory factory, RecordClassDescriptor descriptor, InstrumentMessage message) {

        FixedBoundEncoder encoder = factory.createFixedBoundEncoder(TypeLoaderImpl.DEFAULT_INSTANCE, descriptor);
        MemoryDataOutput out = new MemoryDataOutput();

        long start = System.currentTimeMillis();

        long size = 0;

        for (int i = 0; i < MESSAGES; i++) {
            out.reset();
            encoder.encode(message, out);
            size += out.getSize();
        }

        long total = System.currentTimeMillis() - start;

        System.out.printf ("encoding overall time = %,.3fs; size = %,d bytes \n", total * 0.001, size);

        MemoryDataInput in = new MemoryDataInput(out.getBuffer());
        start = System.currentTimeMillis();

        BoundDecoder decoder = factory.createFixedBoundDecoder(TypeLoaderImpl.DEFAULT_INSTANCE, descriptor);

        for (int i = 0; i < MESSAGES; i++) {
            decoder.decode(in);
            in.seekOffset(0);
        }

        total = System.currentTimeMillis() - start;

        System.out.printf ("decoding overall time = %,.3fs; \n", total * 0.001);
    }
}
