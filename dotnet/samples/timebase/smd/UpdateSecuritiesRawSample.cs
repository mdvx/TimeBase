using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using deltix.qsrv.hf.pub;
using deltix.qsrv.hf.pub.codec;
using deltix.qsrv.hf.pub.md;
using deltix.qsrv.hf.tickdb.pub;
using deltix.qsrv.hf.tickdb.pub.@lock;
using deltix.qsrv.hf.tickdb.pub.query;
using deltix.util.memory;
using java.lang;
using String = System.String;

namespace deltix.samples.timebase.smd
{
    class UpdateSecuritiesRawSample
    {
        /**
   *  A caching factory of encoders and decoders.
   */
        private readonly CodecFactory cfactory =
        CodecFactory.newInterpretingCachingFactory();

        /**
         *  Security stream.
         */
        private DXTickStream stream;

        /**
         *  Data will be loaded from the security stream into this collection.
         */
        private readonly ArrayList data =
            new ArrayList();

        /**
         *  Reusable DataOutputStream-like object for writing data. Required by
         *  message encoders.
         */
        private readonly MemoryDataOutput mdout =
        new MemoryDataOutput();

        /**
         *  Reusable DataInputStream-like object for reading data. Required by
         *  message decoders.
         */
        private readonly MemoryDataInput mdin =
        new MemoryDataInput();

        /**
         *  Reusable buffer for new messages.
         */
        private readonly RawMessage outMsg =
        new RawMessage();

        /**
         *  TimeBase API object for writing data into TimeBase.
         */
        private TickLoader loader;

        public UpdateSecuritiesRawSample()
        {
        }

        public void setStream(DXTickStream stream)
        {
            this.stream = stream;
        }

        public void loadData()
        {
            //
            //  Clear the collection, in case it is reused.
            //
            data.Clear();

            SelectionOptions options = new SelectionOptions();
            //
            //  Read RAW messages. The default is to use the Bound API, which
            //  is not what we want (or need) in this example.
            //
            options.raw = true;
            //
            //  Set up the cursor for very fast initialization. Slightly
            //  sub-optimal read speed is not a factor for reading under 1M
            //  messages.
            //
            options.channelQOS = ChannelQualityOfService.MIN_INIT_TIME;


            InstrumentMessageSource ims =
                stream.select(TimeConstants.TIMESTAMP_UNKNOWN, options, null, null);

            try
            {
                while (ims.next())
                {
                    //
                    //  Make a deep copy of the message. Make sure you do not store
                    //  a reference to the reusable buffer into the array list!
                    //
                    RawMessage msg =
                        (RawMessage)ims.getMessage().copy(true /* deep */);

                    data.Add(msg);
                }
            }
            finally
            {
                ims.close();
            }

            Console.WriteLine("Loaded " + data.Count + " records into memory.");
        }

        public void storeData()
        {
            LoadingOptions options = new LoadingOptions();
            //
            //  Write RAW messages. The default is to use the Bound API, which
            //  is not what we want (or need) in this example.
            //
            options.raw = true;
            //
            //  Set up the loader for very fast initialization. Slightly
            //  sub-optimal write speed is not a factor for writing under 1M
            //  messages.
            //
            options.channelQOS = ChannelQualityOfService.MIN_INIT_TIME;

            loader = stream.createLoader(options);

            try
            {
                foreach (RawMessage oldMsg in data)
                    editAndStoreMessage(oldMsg);
            }
            finally
            {
                loader.close();
                loader = null;
            }
        }

        /**
         *  Override this method to edit data.
         *
         *  @param concreteType     Message type.
         *  @param fieldName        The name of the field.
         *  @param in               Interface for extracting the old value.
         *  @param out              Interface for writing the new value.
         *
         *  @return     true if data has been edited. false if data should be
         *              carried over unchanged.
         */
        public virtual bool editField(
            InstrumentMessage idAndTime,
            RecordClassDescriptor concreteType,
            String fieldName,
            ReadableValue _in,
            WritableValue _out

        )
        {
            return (false);
        }

        public void move(InstrumentMessage msg, UnboundDecoder decoder, UnboundEncoder encoder, RecordClassDescriptor rcd)
        {
            while (decoder.nextField())
            {
                bool encoderHasNext = encoder.nextField();
                String fieldName = decoder.getField().getName();
                //
                //  Encoder and decoder will iterate over fields in exactly the
                //  the same order, because they are created for the same exact
                //  class type. The following assertions illustrate this.
                //
                Debug.Assert(encoderHasNext);
                Debug.Assert(fieldName.Equals(encoder.getField().getName()));

                if (!editField(msg, rcd, fieldName, decoder, encoder))
                    move(msg, decoder.getField().getType(), decoder, encoder);
            }
        }

        public void move(
            InstrumentMessage msg,
            DataType type,
            ReadableValue _in,
            WritableValue _out

        )
        {
            try
            {
                if (type is DateTimeDataType || type is IntegerDataType || type is TimeOfDayDataType)
                {
                    _out.writeLong(_in.getLong());
                }
                else if (type is FloatDataType)
                {
                    FloatDataType ft = (FloatDataType)type;

                    if (ft.isFloat())
                        _out.writeFloat(_in.getFloat());
                    else
                        _out.writeDouble(_in.getDouble());
                }
                else if (type is BooleanDataType)
                {
                    _out.writeBoolean(_in.getBoolean());

                }
                else if (type is BinaryDataType)
                {
                    int size = _in.getBinaryLength();
                    byte[] bin = new byte[size];
                    _in.getBinary(0, size, bin, 0);

                }
                else if (type is ClassDataType)
                {
                    UnboundDecoder decoder = _in.getFieldDecoder();
                    RecordClassDescriptor descriptor = decoder.getClassInfo().getDescriptor();
                    move(msg, _in.getFieldDecoder(), _out.getFieldEncoder(descriptor), descriptor);

                }
                else if (type is ArrayDataType)
                {
                    int length = _in.getArrayLength();
                    _out.setArrayLength(length);

                    for (int i = 0; i < length; i++)
                        move(msg, ((ArrayDataType)type).getElementDataType(), _in.nextReadableElement(), _out.nextWritableElement());
                }
                else
                {
                    _out.writeString(_in.getString());
                }
            }
            catch (NullValueException ex)
            {
                _out.writeNull();
            }
        }

        public void editAndStoreMessage(RawMessage oldMsg)
        {
            RecordClassDescriptor type = oldMsg.type;
            //
            //  Decoder and encoder will be cached at codec factory level.
            //  The following operations are efficient, even though the methods are
            //  called "create...".
            //
            UnboundDecoder decoder =
                cfactory.createFixedUnboundDecoder(type);

            FixedUnboundEncoder encoder =
                cfactory.createFixedUnboundEncoder(type);
            //
            //  Set up mdin to point to the byte array within oldMsg
            //
            oldMsg.setUpMemoryDataInput(mdin);
            //
            //  Set up decoder to read from mdin
            //
            decoder.beginRead(mdin);
            //
            //  Set up encoder to output into mdout
            //
            mdout.reset();
            encoder.beginWrite(mdout);
            //
            //  Move identity and timestamp
            //
            outMsg.type = type;
            outMsg.instrumentType = oldMsg.instrumentType;
            outMsg.symbol = oldMsg.symbol;
            outMsg.timestamp = oldMsg.timestamp;
            //
            //  Iterate over fields making changes if necessary
            //
            while (decoder.nextField())
            {
                bool encoderHasNext = encoder.nextField();
                String fieldName = decoder.getField().getName();
                //
                //  Encoder and decoder will iterate over fields in exactly the
                //  the same order, because they are created for the same exact
                //  class type. The following assertions illustrate this.
                //
                Debug.Assert(encoderHasNext);
                Debug.Assert(fieldName.Equals(encoder.getField().getName()));

                if (!editField(oldMsg, type, fieldName, decoder, encoder))
                    move(outMsg, decoder.getField().getType(), decoder, encoder);
            }
            //
            //  Set up outMsg to point to the byte array within mdout
            //
            outMsg.setBytes(mdout, 0);
            //
            //  Send it to TimeBase
            //
            loader.send(outMsg);
        }

        public void editData()
        {
            DBLock secStreamLock = null;

            //
            //  The securities stream should be locked when data is inserted.
            //  Locking the securities stream achieves two goals:
            //      1) It ensures that no other application can accidentally
            //          corrupt the data being inserted.
            //      2) Removing the lock also notifies interested applications
            //          about the fact that securities data has been changed.
            //          For instance, QuantOffice reloads symbols,
            //          Aggregator checks if market data subscription should be
            //          updated, etc.
            //
            try
            {
                secStreamLock = stream.tryLock(LockType.WRITE, 30000);
                //
                //  Load data from the securities stream into memory.
                //
                loadData();
                //
                //  Clear all data from the stream.
                //
                stream.clear();
                //
                //  Store new copy of the data with required edits.
                //
                storeData();
            }
            finally
            {
                //
                // Release the lock. This will generate the system event message
                // deltix.qsrv.hf.pub.EventMessage, which notifies interested
                // parties that the stream has been updated. This message can be
                // received from system stream
                // deltix.qsrv.hf.tickdb.pub.TickDBFactory.EVENTS_STREAM_NAME
                //
                if (secStreamLock != null)
                    secStreamLock.release();
            }
        }

        public static void Main(String[] args)
        {
            //
            //  Create an instance of the editor that fills out a field called
            //  "brokerID" (must be present in the schema for this to happen)
            //
            UpdateSecuritiesRawSample x =
            new UpdateSecuritiesRawSampleAnonymous();


            DXTickDB tickdb =
                TickDBFactory.createFromUrl("dxtick://localhost");

            tickdb.open(false);

            try
            {
                x.setStream(tickdb.getStream("securities"));

                x.editData();
            }
            finally
            {
                tickdb.close();
            }
        }
    }
    class UpdateSecuritiesRawSampleAnonymous : UpdateSecuritiesRawSample
    {
            public override bool          editField (
                InstrumentMessage       idAndTime,
                RecordClassDescriptor   concreteType,
                String                  fieldName, 
                ReadableValue           _in,
                WritableValue           _out
            ) 
            {
                if (fieldName.Equals ("brokerID")) {
                    _out.writeString ("TRADE." + idAndTime.symbol);
                    return (true);
                }
                else
                    return (false);
            }                              
    }
}
