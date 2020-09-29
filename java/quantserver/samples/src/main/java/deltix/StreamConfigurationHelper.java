package deltix;

import deltix.data.stream.DXChannel;
import deltix.data.stream.MessageDecoder;
import deltix.data.stream.MessageEncoder;
import deltix.qsrv.hf.pub.ExchangeCodec;
import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.TypeLoader;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.qsrv.hf.tickdb.pub.DXTickStream;
import deltix.qsrv.hf.tickdb.pub.TickStream;
import deltix.timebase.MarketMessageType;
import deltix.timebase.messages.*;
import deltix.timebase.messages.calendar.CalendarDateMessage;
import deltix.timebase.messages.calendar.CalendarMessage;
import deltix.timebase.messages.securities.Exchange;
import deltix.timebase.messages.securities.GenericInstrument;
import deltix.timebase.messages.securities.TradingSession;
import deltix.timebase.messages.service.*;
import deltix.util.time.Interval;
import deltix.util.time.Periodicity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class StreamConfigurationHelper {
    
//    /**
//     *  Returned from {@link #getBarSize(deltix.qsrv.hf.tickdb.pub.TickStream)}
//     *  when the stream contains no bar message descriptors.
//     */
//    public static final int                 BS_NO_BARS = -10;

    /**
     *  Returned when bar message descriptor does not contain the barSize field.
     */
    public static final int                 BS_NO_BAR_SIZE = -11;

//    /**
//     *  Returned from {@link #getBarSize(deltix.qsrv.hf.tickdb.pub.TickStream)}
//     *  when the barSize field is non-static.
//     */
//    public static final int                 BS_NON_STATIC = 0;

    public static DataField                 mkField (
        String                                  name,         
        String                                  title,
        DataType                                type,
        String                                  relativeTo,
        Object                                  staticValue
    )
    {
        if (staticValue == null)
            return (new NonStaticDataField (name, title, type, false, relativeTo));
        else {
            String value = staticValue.toString();
            // special handling for ALPHANUMERIC: empty string means <null>
            if (type instanceof VarcharDataType && "ALPHANUMERIC(10)".equals(type.getEncoding())) {
                if (value.length() == 0)
                    value = null;
                else
                    ExchangeCodec.codeToLong((String) staticValue); // validate ALPHANUMERIC(10) 
            }
            return (new StaticDataField(name, title, type, value));
        }
    }

    public static RecordClassDescriptor     mkMarketMessageDescriptor (
        Integer                 staticCurrencyCode
    )
    {
        return mkMarketMessageDescriptor(staticCurrencyCode, true);
    }

    public static RecordClassDescriptor     mkMarketMessageDescriptor (
        Integer                 staticCurrencyCode,
        boolean                 staticOriginalTimestamp
    )
    {
        return mkMarketMessageDescriptor(staticCurrencyCode, staticOriginalTimestamp, null);
    }

    /**
     * Builds RecordClassDescriptor for standard message class MarketMessage
     *
     * @param staticOriginalTimestamp Controls MarketMessage.originalTimestamp: pass <code>null</code> to omit this field, true if you want to make this fields static, false otherwise.
     * @param staticNanoTime Controls MarketMessage.nanoTime: pass <code>null</code> to omit this field, true if you want to make this fields static, false otherwise.
     */
     public static RecordClassDescriptor     mkMarketMessageDescriptor (
        Integer                 staticCurrencyCode,
        Boolean                 staticOriginalTimestamp,
        Boolean                 staticNanoTime
    )
    {
        final String            name = MarketMessage.class.getName ();
        final List<DataField>      fields = new ArrayList<>(4);
        if (staticOriginalTimestamp != null) {
            fields.add (
                staticOriginalTimestamp ?
                    new StaticDataField(
                        "originalTimestamp", "Original Time",
                        new DateTimeDataType(true), null) :
                    new NonStaticDataField(
                        "originalTimestamp", "Original Time",
                        new DateTimeDataType(true))

            );
        }


//nanoTime was removed
        if (staticNanoTime != null) {
             fields.add (
                staticNanoTime ?
                new StaticDataField(
                    "nanoTime", "Nano Time",
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true), null) :
                new NonStaticDataField(
                    "nanoTime", "Nano Time",
                    new IntegerDataType(IntegerDataType.ENCODING_INT64, true))
             );
        }


        fields.add (mkField (
                "currencyCode", "Currency Code",
                new IntegerDataType (IntegerDataType.ENCODING_INT16, true), null,
                staticCurrencyCode
            ));

        fields.add (new NonStaticDataField (
                "sequenceNumber", "Sequence Number",
                new IntegerDataType (IntegerDataType.ENCODING_INT64, true)
            ));
        
        return (new RecordClassDescriptor (name, name, true, null, fields.toArray(new DataField[fields.size()])));
    }

//    public static RecordClassDescriptor mkInstrumentMessageDescriptor ( ) {
//        final String name = InstrumentMessage.class.getName ( );
//        String[] enumValues = new String[InstrumentType.values ( ).length];
//        for (int i = 0; i < InstrumentType.values ( ).length; i++)
//            enumValues[i] = InstrumentType.values ( )[i].toString ( );
//
//        final DataField[] fields =
//        {
//            StreamConfigurationHelper.mkField ( "instrumentType", "Instrument Type",
//                new EnumDataType ( false, new EnumClassDescriptor( InstrumentType.class.getName(), "InstrumentType", enumValues )),
//                null,
//                null),
//            StreamConfigurationHelper.mkField ( "symbol",
//                                                "Symbol",
//                                                new StringDataType ( StringDataType.ENCODING_INLINE_VARSIZE,
//                                                                     false,
//                                                                     false ),
//                                                null,
//                                                null )
//        };
//
//        return (new RecordClassDescriptor (name, name, false, null, fields));
//    }

    public static RecordClassDescriptor    mkMarketMsgSubclassDescriptor (
        RecordClassDescriptor   marketMsgDescriptor,
        String                  name, 
        Integer                 staticCurrencyCode, 
        DataField []            fields
    )
    {
        if (marketMsgDescriptor == null)
            marketMsgDescriptor = 
                mkMarketMessageDescriptor (staticCurrencyCode);
        
        return new RecordClassDescriptor (
            name, name, false, 
            marketMsgDescriptor, 
            fields
        );
    }

    public static RecordClassDescriptor     mkEventMessageDescriptor ()
    {
        final String            name = EventMessage.class.getName ();

        final DataField []      fields = {
            new NonStaticDataField ("eventType", "EventType",
                    new EnumDataType(true, new EnumClassDescriptor(EventMessageType.class))),
        };

        return new RecordClassDescriptor ( name, name, false, null, fields);
    }

// **Moved to BinaryMessage.DESCRIPTOR
//    public static RecordClassDescriptor     mkDataLossMessageDescriptor()
//    {
//        final String            name = DataLossMessage.class.getName ();
//
//        final DataField []      fields = {
//            new NonStaticDataField ("bytes", "Bytes Lost", new IntegerDataType(IntegerDataType.ENCODING_INT64, false)),
//            new NonStaticDataField ("fromTime", "From Time", new IntegerDataType(IntegerDataType.ENCODING_INT64, true)),
//        };
//
//        return new RecordClassDescriptor (name, name, false, null, fields);
//    }
//
// ***     Moved to BinaryMessage.DESCRIPTOR
//    public static RecordClassDescriptor     mkBinaryMessageDescriptor()
//    {
//        final String            name = BinaryMessage.class.getName ();
//
//        final DataField []      fields = {
//            new NonStaticDataField ("data", "Data buffer", BinaryDataType.getDefaultInstance())
//        };
//
//        return new RecordClassDescriptor (name, name, false, null, fields);
//    }
    
    public static RecordClassDescriptor     mkBarMessageDescriptor (
        RecordClassDescriptor   marketMsgDescriptor,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,        
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        final String            name = BarMessage.class.getName ();
        
        final DataField []      fields = {
            mkField (
                "exchangeId", "Exchange Code",
                new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                staticExchangeCode
            ),
//            mkField (
//                "barSize", "Bar Size",
//                new IntegerDataType (IntegerDataType.ENCODING_PINTERVAL, false), null,
//                staticBarSize
//            ),
            new NonStaticDataField ("close", "Close", new FloatDataType (priceEncoding, true)),
            new NonStaticDataField ("open", "Open", new FloatDataType (priceEncoding, true), "close"),
            new NonStaticDataField ("high", "High", new FloatDataType (priceEncoding, true), "close"),
            new NonStaticDataField ("low", "Low", new FloatDataType (priceEncoding, true), "close"),
            new NonStaticDataField ("volume", "Volume", new FloatDataType (sizeEncoding, true))
        };
        
        return (mkMarketMsgSubclassDescriptor (marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    public static RecordClassDescriptor     mkTradeMessageDescriptor (
        RecordClassDescriptor   marketMsgDescriptor,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        final String            name = TradeMessage.class.getName ();
        
        final DataField []      fields = {
            mkField (
                "exchangeId", "Exchange Code",
                new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                staticExchangeCode
            ),
            new NonStaticDataField ("price", "Price", new FloatDataType (priceEncoding, true)),
            new NonStaticDataField ("size", "Size", new FloatDataType (sizeEncoding, true)),
            new NonStaticDataField ("condition", "Trade Condition", new VarcharDataType ("UTF8", true, false)),
            new NonStaticDataField ("aggressorSide", "Aggressor Side", new EnumDataType (true, new EnumClassDescriptor(AggressorSide.class))),
            //new NonStaticDataField ("beginMatch", "Begin Match", new BooleanDataType (true)),
            new NonStaticDataField ("netPriceChange", "Net Price Change", new FloatDataType (priceEncoding, true)),
            new NonStaticDataField ("eventType", "Event Type", new EnumDataType (true, new EnumClassDescriptor(MarketEventType.class)))
        };
        
        return (mkMarketMsgSubclassDescriptor (marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    public static RecordClassDescriptor mkPacketEndMessageDescriptor(
            RecordClassDescriptor parentMsgDescriptor,
            String name,
            String description,
            DataField[] fields
    ) {

        return new RecordClassDescriptor(
                name,
                description,
                false,
                parentMsgDescriptor,
                fields
        );
    }
    
    public static RecordClassDescriptor     mkBBOMessageDescriptor (
        RecordClassDescriptor   marketMsgDescriptor,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        final String            name = BestBidOfferMessage.class.getName ();
        
        final DataField []      fields = {
            mkField (
                "isNational", "National BBO",
                new BooleanDataType (true), null,
                isNational
            ),
            new NonStaticDataField ("bidPrice", "Bid Price", new FloatDataType (priceEncoding, true)),
            new NonStaticDataField ("bidSize", "Bid Size", new FloatDataType (sizeEncoding, true)),
            mkField (
                "bidExchangeId", "Bid Exchange",
                new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                staticExchangeCode
            ),
            new NonStaticDataField ("offerPrice", "Offer Price", new FloatDataType (priceEncoding, true)),
            new NonStaticDataField ("offerSize", "Offer Size", new FloatDataType (sizeEncoding, true)),
            mkField (
                "offerExchangeId", "Offer Exchange",
                new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                staticExchangeCode
            ),
        };
        
        return (mkMarketMsgSubclassDescriptor (marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    private static final EnumClassDescriptor BOOK_UPDATE_ACTION_DESCRIPTOR = new EnumClassDescriptor(BookUpdateAction.class);

    public static RecordClassDescriptor     mkLevel2MessageDescriptor (
        RecordClassDescriptor   marketMsgDescriptor,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        final String            name = Level2Message.class.getName ();

        final DataField[] fields = {
                new NonStaticDataField("price", "Price", new FloatDataType(priceEncoding, true)),
                new NonStaticDataField("size", "Size", new FloatDataType(sizeEncoding, true)),
                mkField(
                        "exchangeId", "Exchange Code",
                        new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                        staticExchangeCode
                ),
                new NonStaticDataField("depth", "Market Depth", new IntegerDataType(IntegerDataType.ENCODING_INT8, true)),
                new NonStaticDataField("isAsk", "Is Ask", new BooleanDataType(false)),
                new NonStaticDataField("action", "Book Update Action", new EnumDataType(true, BOOK_UPDATE_ACTION_DESCRIPTOR)),
                new NonStaticDataField("isLast", "End of Package", new BooleanDataType(false)),
                new NonStaticDataField("numOfOrders", "Number Of Orders", new IntegerDataType(IntegerDataType.ENCODING_INT32, true))
        };

        return (mkMarketMsgSubclassDescriptor (marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    public static RecordClassDescriptor     mkLevel2ActionDescriptor (
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        final String            name = Level2Action.class.getName ();

        final DataField[] fields = {
                new NonStaticDataField("level", "Level", new IntegerDataType(IntegerDataType.ENCODING_INT16, false)),
                new NonStaticDataField("isAsk", "Is Ask", new BooleanDataType(false)),
                new NonStaticDataField("action", "Book Update Action", new EnumDataType(true, BOOK_UPDATE_ACTION_DESCRIPTOR)),
                new NonStaticDataField("price", "Price", new FloatDataType(priceEncoding, true)),
                new NonStaticDataField("size", "Size", new FloatDataType(sizeEncoding, true)),
                new NonStaticDataField("numOfOrders", "Number Of Orders", new IntegerDataType(IntegerDataType.ENCODING_INT32, true)),
                new NonStaticDataField("quoteId", "Number Of Orders", new VarcharDataType(VarcharDataType.ENCODING_INLINE_VARSIZE, true, true))
        };

        return new RecordClassDescriptor (
                name, name, false,
                null,
                fields
        );

        //return (mkMarketMsgSubclassDescriptor (marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    public static RecordClassDescriptor     mkLevel2IncrementMessageDescriptor (
            RecordClassDescriptor   marketMsgDescriptor,
            String                  staticExchangeCode,
            Integer                 staticCurrencyCode,
            RecordClassDescriptor   level2ActionDescriptor
    )
    {
        final String name = L2Message.class.getName();

        final DataField[] fields = {
                mkField(
                        "exchangeId", "Exchange Code",
                        new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
                        staticExchangeCode),
                new NonStaticDataField("isImplied", "Is Implied", new BooleanDataType(true)),
                new NonStaticDataField("isSnapshot", "Is Snapshot", new BooleanDataType(true)),
                new StaticDataField("maxDepth", "Max Depth", new IntegerDataType(IntegerDataType.ENCODING_INT16, true), null),
                new NonStaticDataField(
                        "sequenceId",
                        "Sequence ID",
                        new IntegerDataType(IntegerDataType.ENCODING_INT64, true)),
                new NonStaticDataField(
                        "actions",
                        "Actions",
                        new ArrayDataType(true, new ClassDataType(true, level2ActionDescriptor))
                ),
        };

        return (mkMarketMsgSubclassDescriptor(marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    public static RecordClassDescriptor     mkL2SnapshotMessageDescriptor (
        RecordClassDescriptor   marketMsgDescriptor,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        final String name = L2SnapshotMessage.class.getName();
        final DataField[] fields = new DataField[3 + 20 + 10];

        int idx = 0;
        fields[idx++] = mkField(
            "exchangeId", "Exchange Code",
            new VarcharDataType(VarcharDataType.getEncodingAlphanumeric(10), true, false), null,
            staticExchangeCode);
        fields[idx++] = new NonStaticDataField("isAsk", "Is Ask", new BooleanDataType(false));
        fields[idx++] = new NonStaticDataField("maxMarketDepth", "Market Depth", new IntegerDataType(IntegerDataType.ENCODING_INT8, true));

        for (int i = 0; i < 10; i++) {
            fields[idx++] = new NonStaticDataField("price" + i, "Price " + i, new FloatDataType(priceEncoding, true));
            fields[idx++] = new NonStaticDataField("size" + i, "Size " + i, new FloatDataType(sizeEncoding, true));
        }

        for (int i = 0; i < 10; i++) {
            fields[idx++] = new NonStaticDataField("numOfOrders" + i, "Number Of Orders " + i, new IntegerDataType(IntegerDataType.ENCODING_INT32, true));
        }
        
        return (mkMarketMsgSubclassDescriptor(marketMsgDescriptor, name, staticCurrencyCode, fields));
    }

    public static void      setBarNoExchNoCur (
        DXTickStream stream,
        Interval                periodicity
    )
    {
        setBar (stream, "", 999, periodicity);
    }
    
    public static void      setBar (
        DXTickStream            stream,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        Interval                periodicity
    )
    {
        setBar (
            stream, staticExchangeCode, staticCurrencyCode, periodicity,
            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
        );
    }
    
    public static void      setBar (
        DXTickStream            stream,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        Interval                periodicity,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        stream.setFixedType (
            mkBarMessageDescriptor (
                null,
                staticExchangeCode, 
                staticCurrencyCode,
                priceEncoding,
                sizeEncoding
            )
        );

        if (periodicity != null)
            stream.setPeriodicity(Periodicity.mkRegular(periodicity));
    }
    
    public static void      setTradeNoExchNoCur (
        DXTickStream            stream
    )
    {
        setTrade (stream, "", 999);
    }
    
    public static void      setTrade (
        DXTickStream            stream,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode
    )
    {
        setTrade (
            stream, staticExchangeCode, staticCurrencyCode, 
            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
        );
    }
    
    public static void      setTrade (
        DXTickStream            stream,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        stream.setFixedType (
            mkTradeMessageDescriptor (
                null,
                staticExchangeCode, 
                staticCurrencyCode, 
                priceEncoding,
                sizeEncoding
            )
        );
    }
    
    public static void      setBBONoExchNoCur (
        DXTickStream            stream
    )
    {
        setBBO (stream, true, null, 999);
    }
    
    public static void      setBBO (
        DXTickStream            stream,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode
    )
    {
        setBBO (
            stream, isNational, staticExchangeCode, staticCurrencyCode, 
            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
        );
    }
    
    public static void      setBBO (
        DXTickStream            stream,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        stream.setFixedType (
            mkBBOMessageDescriptor (
                null,
                isNational,
                staticExchangeCode, 
                staticCurrencyCode, 
                priceEncoding,
                sizeEncoding
            )
        );
    }
    
    public static void      setTradeOrBBO (
        DXTickStream            stream,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode
    )
    {
        setTradeOrBBO (
            stream, isNational, staticExchangeCode, staticCurrencyCode, 
            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
        );
    }
    
    public static void      setTradeOrBBO (
        DXTickStream            stream,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode,
        String                  priceEncoding,
        String                  sizeEncoding
    )
    {
        RecordClassDescriptor   marketMsgDescriptor = 
                mkMarketMessageDescriptor (staticCurrencyCode);
        
        stream.setPolymorphic (
            mkTradeMessageDescriptor (
                marketMsgDescriptor,
                staticExchangeCode, 
                staticCurrencyCode, 
                priceEncoding,
                sizeEncoding
            ),
            mkBBOMessageDescriptor (
                marketMsgDescriptor,
                isNational,
                staticExchangeCode, 
                staticCurrencyCode, 
                priceEncoding,
                sizeEncoding
            )
        );
    }   
    
    public static void      setFromTemplate (DXTickStream stream, DXTickStream template) {
        if (template.isFixedType ())
            stream.setFixedType (template.getFixedType ());
        else
            stream.setPolymorphic (template.getPolymorphicDescriptors ());
        
        stream.setPeriodicity(template.getPeriodicity());
    }

    public static RecordClassDescriptor     mkUniversalTradeMessageDescriptor () {
        return (
            mkTradeMessageDescriptor (
                null,
                null,
                null,
                FloatDataType.ENCODING_SCALE_AUTO,
                FloatDataType.ENCODING_SCALE_AUTO
            )
        );
    }

    public static RecordClassDescriptor     mkUniversalBarMessageDescriptor () {
        return (
            mkBarMessageDescriptor (
                null,
                null,
                null,
                FloatDataType.ENCODING_SCALE_AUTO,
                FloatDataType.ENCODING_SCALE_AUTO
            )
        );
    }

    public static RecordClassDescriptor     mkUniversalBBOMessageDescriptor () {
        return (
            mkBBOMessageDescriptor (
                null,
                null,
                null,
                null,
                FloatDataType.ENCODING_SCALE_AUTO,
                FloatDataType.ENCODING_SCALE_AUTO
            )
        );
    }

    public static RecordClassDescriptor     mkUniversalDescriptor (Class <?> cls) {
        String      cname = cls.getName ();

        if (cname.equals (BarMessage.class.getName ()))
            return (mkUniversalBarMessageDescriptor ());

        if (cname.equals (TradeMessage.class.getName ()))
            return (mkUniversalTradeMessageDescriptor ());

        if (cname.equals (BestBidOfferMessage.class.getName ()))
            return (mkUniversalBBOMessageDescriptor ());

        throw new IllegalArgumentException ("Don't know how to describe class " + cname);
    }

    public static RecordClassDescriptor[]  mkUniversalMarketDescriptors (MarketMessageType[] types) {
        List<RecordClassDescriptor> descriptors = new ArrayList<RecordClassDescriptor>();
        RecordClassDescriptor   marketMsgDescriptor = mkMarketMessageDescriptor (null);

        for (MarketMessageType type : types) {
            if (type == MarketMessageType.TYPE_BAR)
                descriptors.add( mkBarMessageDescriptor (
                                    marketMsgDescriptor,
                                    null,
                                    null,
                                    FloatDataType.ENCODING_SCALE_AUTO,
                                    FloatDataType.ENCODING_SCALE_AUTO
                                ));
            else if (type == MarketMessageType.TYPE_BBO)
                descriptors.add( mkBBOMessageDescriptor (
                                    marketMsgDescriptor,
                                    null,
                                    null, 
                                    null,
                                    FloatDataType.ENCODING_SCALE_AUTO,
                                    FloatDataType.ENCODING_SCALE_AUTO
                                ));
            else if (type == MarketMessageType.TYPE_TRADE)
                descriptors.add(mkTradeMessageDescriptor (
                                    marketMsgDescriptor,
                                    null,
                                    null,
                                    FloatDataType.ENCODING_SCALE_AUTO,
                                    FloatDataType.ENCODING_SCALE_AUTO
                                ));
        }

        return descriptors.toArray(new RecordClassDescriptor[descriptors.size()]);
    }

    public static RecordClassDescriptor []  mkUniversalMarketDescriptors () {
        RecordClassDescriptor   marketMsgDescriptor = 
                mkMarketMessageDescriptor (null);
        
        return (
            new RecordClassDescriptor [] {
                mkTradeMessageDescriptor (
                    marketMsgDescriptor,
                    null, 
                    null,
                    FloatDataType.ENCODING_SCALE_AUTO,
                    FloatDataType.ENCODING_SCALE_AUTO
                ),
                mkBBOMessageDescriptor (
                    marketMsgDescriptor,
                    null,
                    null, 
                    null,
                    FloatDataType.ENCODING_SCALE_AUTO,
                    FloatDataType.ENCODING_SCALE_AUTO
                ),
                mkBarMessageDescriptor (
                    marketMsgDescriptor,
                    null, 
                    null,                    
                    FloatDataType.ENCODING_SCALE_AUTO,
                    FloatDataType.ENCODING_SCALE_AUTO
                )
            }
        );
    }
    
    public static void      setUniversalMarket (DXTickStream stream) {                
        stream.setPolymorphic (mkUniversalMarketDescriptors ());
    }

    public static boolean   containsSubclassesOnly (RecordClassDescriptor[] rcds, Class <?> cls) {
        final String    javaClassName = cls.getName ();

        for (RecordClassDescriptor rcd : rcds)
            if (!rcd.isConvertibleTo (javaClassName))
                return (false);

        return (true);
    }
        
    public static boolean   containsSubclassesOnly (RecordClassDescriptor[] rcds,final String newPath,final String oldPath) {
        for (RecordClassDescriptor rcd : rcds)
            if (!rcd.isConvertibleTo (newPath)  && !rcd.isConvertibleTo (oldPath))
                return (false);

        return (true);
    }
        
    public static boolean   containsSubclassesOnly (TickStream stream, Class <?> cls) {
        final String    javaClassName = cls.getName ();
        
        if (stream.isPolymorphic ()) {
            for (RecordClassDescriptor rcd : stream.getPolymorphicDescriptors ())
                if (!rcd.isConvertibleTo (javaClassName))
                    return (false);
            
            return (true);
        }
        else 
            return (stream.getFixedType ().isConvertibleTo (javaClassName));       
    }

    public static boolean   containsMarketMessagesOnly (TickStream ... streams) {
        for (TickStream stream : streams) {
            if (!(containsSubclassesOnly (stream, MarketMessage.class)))
                return false;
        }
        return true;
    }
    
    public static boolean   containsMarketMessagesOnly (TickStream stream) {
        return (containsSubclassesOnly (stream, MarketMessage.class));
    }
    
    public static boolean   isFixedBuiltIn (TickStream stream, Class <?> cls) {
        return (
            stream.isFixedType () && 
            cls.getName ().equals (stream.getFixedType ().getName ())
        );
    }
    
    public static boolean   mayContainSubclasses (TickStream stream, Class <?> cls) {
        final String    javaClassName = cls.getName ();

        if (stream.isPolymorphic ()) {
            for (RecordClassDescriptor rcd : stream.getPolymorphicDescriptors ())
                if (rcd.isConvertibleTo (javaClassName))
                    return (true);
            
            return (false);
        }
        else
            return (stream.getFixedType ().isConvertibleTo (javaClassName));
    }

    public static boolean   mayContainSubclasses (TickStream stream, Class <?> ... cls) {
        for (int i = 0; i < cls.length; i++)
            if (mayContainSubclasses(stream, cls[i]))
                return true;

        return false;
    }

    public static boolean   mayContainSubclasses (DXChannel channel, Class <?> cls) {
        final String    javaClassName = cls.getName ();

        for (RecordClassDescriptor rcd : channel.getTypes ())
            if (rcd.isConvertibleTo (javaClassName))
                return true;

        return false;
    }

    public static boolean   containsTradeMessagesOnly (TickStream stream) {
        return (isFixedBuiltIn (stream, TradeMessage.class));
    }
    
    public static boolean   containsBBOMessagesOnly (TickStream stream) {
        return (isFixedBuiltIn (stream, BestBidOfferMessage.class));
    }
    
    public static boolean   containsBarMessagesOnly (TickStream stream) {
        return (isFixedBuiltIn (stream, BarMessage.class));
    }
    
    public static boolean   mayContainTradeMessages (TickStream stream) {
        return (mayContainSubclasses (stream, TradeMessage.class));
    }

    public static boolean   mayContainLevel2Messages (TickStream stream) {
        return (mayContainSubclasses (stream, Level2Message.class, L2Message.class ));
    }

    public static boolean   mayContainL2Descriptors(TickStream stream) {
        ClassDescriptor[] descriptors = stream.getAllDescriptors();
        HashSet<String> l2Descriptors = new HashSet<String>(
                Arrays.asList(
                        L2SnapshotMessage.class.getName(),
                        L2Message.class.getName(),
                        Level2Message.class.getName(),
                        TradeMessage.class.getName()
                )
        );
        if (descriptors != null){
            for (ClassDescriptor descriptor: descriptors){
                if (l2Descriptors.contains(descriptor.getName())){
                    l2Descriptors.remove(descriptor.getName());
                }
            }
        }

        return descriptors != null && l2Descriptors.isEmpty();
    }

    public static boolean   mayContainBBOMessages (TickStream stream) {
        return (mayContainSubclasses (stream, BestBidOfferMessage.class));
    }

    public static boolean   mayContainBarMessages (TickStream stream) {
        return (mayContainSubclasses (stream, BarMessage.class));
    }

    public static RecordClassDescriptor[] getClassDescriptors(DXTickStream stream) {
        if (stream.isFixedType())
            return new RecordClassDescriptor[] {stream.getFixedType()};
        else
            return stream.getPolymorphicDescriptors();
    }

    public static Interval getPeriodicity (DXChannel channel) {
        if (channel instanceof DXTickStream)
            return ((DXTickStream)channel).getPeriodicity().getInterval();
        else
            return Periodicity.mkIrregular().getInterval();
    }

    //TODO: Remove me once DXTickDB API get similar method
    public static DXChannel<InstrumentMessage> getChannel (DXTickDB db, String key) {
        if (key == null)
            return null;

        DXChannel<InstrumentMessage> result = db.getStream(key);
        if (result == null) {
            for (DXChannel channel : db.listChannels())
                if (key.equals(channel.getKey()))
                    return channel;

        }
        return result;
    }

//    @Deprecated
//    public static long      getBarSize (TickStream stream) {
//        Interval interval = ((DXTickStream) stream).getPeriodicity();
//        if (interval != null) {
//            if (!interval.getUnit().isVariableSize())
//                return interval.toMilliseconds();
//
//            throw new IllegalStateException("Cannot convert variable interval to long");
//        }
//
//        return BS_NO_BAR_SIZE;
//    }

//    public static long      getBarSize (RecordClassDescriptor rcd) {
//        if (!rcd.isConvertibleToJavaClass (BarMessage.class.getName ()))
//            return (BS_NO_BARS);
//
//        DataField       df = rcd.getField ("barSize");
//
//        if (df == null)
//            return (BS_NO_BAR_SIZE);
//
//        if (!(df instanceof StaticDataField))
//            return (BS_NON_STATIC);
//
//        return (DataType.parseLong (((StaticDataField) df).getStaticValue ()));
//    }
//
//    /**
//     *  Analyze bar message size in the specified stream.
//     *
//     *  @param stream   The stream to analyze.
//     *  @return         The static bar size value, or one of the BS_ constants.
//     */
//    public static long      getBarSize (TickStream stream) {
//        if (stream.isPolymorphic ()) {
//            for (RecordClassDescriptor rcd : stream.getPolymorphicDescriptors ()) {
//                final long s = getBarSize(rcd);
//
//                if (s != BS_NO_BARS)
//                    return (s);
//            }
//
//            return (BS_NO_BARS);
//        }
//        else
//            return (getBarSize (stream.getFixedType ()));
//    }

	public static void setTradeOrBBOOrBar(
        DXTickStream            stream,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode        
    )
    {
        RecordClassDescriptor   marketMsgDescriptor = 
            mkMarketMessageDescriptor (staticCurrencyCode);
    
	    stream.setPolymorphic (
	        mkTradeMessageDescriptor (
	            marketMsgDescriptor,
	            staticExchangeCode, 
	            staticCurrencyCode, 
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        ),
	        mkBBOMessageDescriptor (
	            marketMsgDescriptor,
                isNational,
	            staticExchangeCode, 
	            staticCurrencyCode, 
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        ),
	        mkBarMessageDescriptor (
	            marketMsgDescriptor,
	            staticExchangeCode, 
	            staticCurrencyCode, 	            
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        )
	    );
		
	}

	public static void setBBOOrBar(
        DXTickStream            stream,
        Boolean                 isNational,
        String                  staticExchangeCode,
        Integer                 staticCurrencyCode
    )
    {
        RecordClassDescriptor   marketMsgDescriptor = 
            mkMarketMessageDescriptor (staticCurrencyCode);
    
	    stream.setPolymorphic (
	        mkBBOMessageDescriptor (
	            marketMsgDescriptor,
                isNational,
	            staticExchangeCode, 
	            staticCurrencyCode, 
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        ),
	        mkBarMessageDescriptor (
	            marketMsgDescriptor,
	            staticExchangeCode, 
	            staticCurrencyCode,	            
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        )
	    );
	}

	public static void setTradeOrBar(DXTickStream stream, String staticExchangeCode, Integer staticCurrencyCode) {
        RecordClassDescriptor   marketMsgDescriptor = 
            mkMarketMessageDescriptor (staticCurrencyCode);
    
	    stream.setPolymorphic (
	        mkTradeMessageDescriptor (
	            marketMsgDescriptor,
	            staticExchangeCode, 
	            staticCurrencyCode, 
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        ),
	        mkBarMessageDescriptor (
	            marketMsgDescriptor,
	            staticExchangeCode, 
	            staticCurrencyCode,
	            FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO
	        )
	    );
	}

//    public static void      setMeta (
//        DXTickStream            stream
//    )
//    {
//	    stream.setPolymorphic (
//            mkSecurityMetaInfoDescriptors()
//	    );
//    }

//    public static RecordClassDescriptor[] mkSecurityMetaInfoDescriptors() {
//        Introspector ix = Introspector.createEmptyMessageIntrospector();
//        for (Class<?> clazz : GenericInstrumentHelper.getInstruments()) {
//            try {
//                ix.introspectRecordClass("secMd", clazz);
//            } catch (Introspector.IntrospectionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return ix.getRecordClasses(GenericInstrument.class);
//    }

    public static RecordClassDescriptor[] getStandardMarketMessageDescriptors () {

            final Integer staticCurrency = 840; //USD

            RecordClassDescriptor abstractMarketMessage = StreamConfigurationHelper.mkMarketMessageDescriptor(staticCurrency, null, null);

        RecordClassDescriptor[] classicMessagesDescriptors = new RecordClassDescriptor[] {
                    StreamConfigurationHelper.mkTradeMessageDescriptor(abstractMarketMessage, null, staticCurrency, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO),
                    StreamConfigurationHelper.mkBBOMessageDescriptor(abstractMarketMessage, true, null, staticCurrency, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO),
                    StreamConfigurationHelper.mkBarMessageDescriptor(abstractMarketMessage, null, staticCurrency, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO),
                    StreamConfigurationHelper.mkLevel2MessageDescriptor(abstractMarketMessage, null, staticCurrency, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO),
                    StreamConfigurationHelper.mkL2SnapshotMessageDescriptor(abstractMarketMessage, null, staticCurrency, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO)
            };

        return classicMessagesDescriptors;
    }

    public static RecordClassDescriptor[]   mkTradingCalendarDescriptors() {
        try {
            Introspector it = Introspector.createEmptyMessageIntrospector();
            it.register(getStandardMarketMessageDescriptors());
            List<RecordClassDescriptor> cds = new ArrayList<RecordClassDescriptor>();

            cds.add(it.introspectRecordClass(CalendarMessage.class));
            cds.add(it.introspectRecordClass(CalendarDateMessage.class));

            return cds.toArray(new RecordClassDescriptor[cds.size()]);

        } catch (Introspector.IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static RecordClassDescriptor mkTradingSessionDescriptor() {
        try {
            return (RecordClassDescriptor) Introspector.introspectSingleClass(TradingSession.class);
        } catch (Introspector.IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static RecordClassDescriptor mkExchangeDescriptor() {
        try {
            return (RecordClassDescriptor) Introspector.introspectSingleClass(Exchange.class);
        } catch (Introspector.IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static final RecordClassDescriptor DATA_LOSS_MESSAGE_DESCRIPTOR = new RecordClassDescriptor(
            DataLossMessage.class,
    null,
            new DataField[] {
                new NonStaticDataField("bytes", "Bytes Lost", new IntegerDataType(IntegerDataType.ENCODING_INT64, false)),
                new NonStaticDataField("fromTime", "From Time", new DateTimeDataType(true))
            }
    );

    public static final RecordClassDescriptor STREAM_TRUNCATED_MESSAGE_DESCRIPTOR = new RecordClassDescriptor (
            StreamTruncatedMessage.class,
    null,
            new DataField[] {
                new NonStaticDataField("version", "Version", new IntegerDataType(IntegerDataType.ENCODING_INT64, false)),
                new NonStaticDataField("nanoTime", "Truncation Time", new IntegerDataType(IntegerDataType.ENCODING_INT64, true)),
                new NonStaticDataField("instruments", "Instruments", new VarcharDataType(VarcharDataType.ENCODING_INLINE_VARSIZE, true, true))
            }
    );

    public static final RecordClassDescriptor META_DATA_CHANGE_MESSAGE_DESCRIPTOR = new RecordClassDescriptor (
            MetaDataChangeMessage.CLASS_NAME, MetaDataChangeMessage.CLASS_NAME, false, null,
            new NonStaticDataField ("version", "Version", new IntegerDataType(IntegerDataType.ENCODING_INT64, true)),
            new NonStaticDataField ("converted", "Converted", new BooleanDataType(false))
    );

    public static final RecordClassDescriptor REAL_TIME_START_MESSAGE_DESCRIPTOR = new RecordClassDescriptor (
            RealTimeStartMessage.DESCRIPTOR_GUID, RealTimeStartMessage.CLASS_NAME, RealTimeStartMessage.CLASS_NAME, false, null);


    public static final RecordClassDescriptor BINARY_MESSAGE_DESCRIPTOR = new RecordClassDescriptor(
            BinaryMessage.class,
            null,
            new DataField[] {
                new NonStaticDataField("data", "Data buffer", BinaryDataType.getDefaultInstance())
            }
    );

    public static final RecordClassDescriptor ERROR_MESSAGE_DESCRIPTOR = new RecordClassDescriptor(
            ErrorMessage.DESCRIPTOR_GUID, ErrorMessage.CLASS_NAME, ErrorMessage.CLASS_NAME, false, null,
            new NonStaticDataField("errorType",     "Type",     VarcharDataType.getDefaultInstance()),
            new NonStaticDataField("seqNum",        "Sequence Number",    new IntegerDataType(IntegerDataType.ENCODING_INT64, false, null, null)),
            new NonStaticDataField("level",         "Level",    new EnumDataType(true, new EnumClassDescriptor(ErrorLevel.class))),
            new NonStaticDataField("messageText",   "Text",     VarcharDataType.getDefaultInstance()),
            new NonStaticDataField("details",       "Details",  VarcharDataType.getDefaultInstance())
    );

}
