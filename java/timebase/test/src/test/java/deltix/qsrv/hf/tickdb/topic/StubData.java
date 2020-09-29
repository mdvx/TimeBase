package deltix.qsrv.hf.tickdb.topic;

import deltix.qsrv.hf.pub.md.DataField;
import deltix.qsrv.hf.pub.md.FloatDataType;
import deltix.qsrv.hf.pub.md.IntegerDataType;
import deltix.qsrv.hf.pub.md.NonStaticDataField;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.StreamConfigurationHelper;

/**
 * @author Alexei Osipov
 */
class StubData {


    static RecordClassDescriptor makeTradeMessageDescriptor() {
        RecordClassDescriptor marketMsgDescriptor = mkMarketMessageDescriptor(840);


        return (StreamConfigurationHelper.mkTradeMessageDescriptor(marketMsgDescriptor, "", 840, FloatDataType.ENCODING_SCALE_AUTO, FloatDataType.ENCODING_SCALE_AUTO));
    }

    private static RecordClassDescriptor mkMarketMessageDescriptor(
            Integer staticCurrencyCode
    ) {
        final String name = deltix.timebase.api.messages.MarketMessage.class.getName();
        final DataField[] fields = {
                new NonStaticDataField(
                        "originalTimestamp", "Original Time",
                        new IntegerDataType(IntegerDataType.ENCODING_INT64, false)),
                StreamConfigurationHelper.mkField(
                        "currencyCode", "Currency Code",
                        new IntegerDataType(IntegerDataType.ENCODING_INT16, true), null,
                        staticCurrencyCode
                )
        };

        return (new RecordClassDescriptor(name, name, true, null, fields));
    }
}
