package deltix.qsrv.hf.tickdb.pub.mapreduce;

import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.TypeLoaderImpl;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.codec.FixedBoundEncoder;
import deltix.qsrv.hf.pub.codec.RecordTypeMap;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.comm.TDBProtocol;
import deltix.qsrv.hf.tickdb.impl.SimpleMessageDecoder;
import deltix.util.lang.Util;
import deltix.util.memory.MemoryDataInput;
import deltix.util.memory.MemoryDataOutput;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.JobContext;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class WritableInstrumentMessage implements WritableMessage<InstrumentMessage>, WritableComparable<InstrumentMessage> {
    private String                      symbol;
    private long                        nanos;
    private MemoryDataOutput            mdo = new MemoryDataOutput();
    private MemoryDataInput             mdi = new MemoryDataInput();

    private int                         code = -1;
    private RecordClassDescriptor[]     types;
    private RecordTypeMap<Class>        typeMap;
    private FixedBoundEncoder[]         encoders;
    private SimpleMessageDecoder[]      decoders;

    public                      WritableInstrumentMessage() {
    }

    @Override
    public void                 set(InstrumentMessage message, JobContext context) throws IOException {
        if (types == null)
            initTypes(context);

        code = typeMap.getCode(message.getClass());
        symbol = message.getSymbol().toString();
        nanos = message.getNanoTime();

        mdo.reset();
        encoders[code].encode(message, mdo);
    }

    @Override
    public InstrumentMessage    get(JobContext context) throws IOException {
        if (types == null)
            initTypes(context);

        InstrumentMessage message = decoders[code].decode(mdi, mdi.getLength());
        message.setSymbol(symbol);
        message.setNanoTime(nanos);
        return message;
    }

    public void                 write(DataOutput dataOutput) throws IOException {
        WritableUtils.writeVInt(dataOutput, code);
        WritableUtils.writeString(dataOutput, symbol);
        WritableUtils.writeVLong(dataOutput, nanos);
        WritableUtils.writeCompressedByteArray(dataOutput, mdo.getBuffer());
    }

    public void                 readFields(DataInput dataInput) throws IOException {
        code = WritableUtils.readVInt(dataInput);
        symbol = WritableUtils.readString(dataInput);
        nanos = WritableUtils.readVLong(dataInput);
        mdi.setBytes(WritableUtils.readCompressedByteArray(dataInput));
    }

    private void                initTypes(JobContext context) throws IOException {
        types = TDBProtocol.readClassSet(context.getConfiguration().get(MAPPER_OUTPUT_TYPE)).getContentClasses();

        this.encoders = new FixedBoundEncoder[types.length];
        this.decoders = new SimpleMessageDecoder[types.length];
        final Class<?>[] classes = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            encoders[i] = CodecFactory.COMPILED.createFixedBoundEncoder(TypeLoaderImpl.DEFAULT_INSTANCE, types[i]);
            decoders[i] = new SimpleMessageDecoder(TypeLoaderImpl.DEFAULT_INSTANCE, CodecFactory.COMPILED, types[i]);
            classes[i] = encoders[i].getClassInfo().getTargetClass();
        }

        typeMap = new RecordTypeMap<Class>(classes);
    }

    @Override
    public int                  compareTo(InstrumentMessage o) {
        if (nanos == o.getNanoTime())
            return  Util.fastCompare(symbol, o.getSymbol());

        return nanos > o.getNanoTime() ? 1 : -1;
    }
}
