package deltix.samples.timebase.basics;

import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.pub.*;

public class ExtendedBarSample {

    public static final String STREAM_KEY = "custom";

    public static DXTickStream createSampleStream (DXTickDB db) {
        DXTickStream            stream = db.getStream (STREAM_KEY);

        if (stream == null) {

            // create default BarMessage descriptor
            RecordClassDescriptor barMessageDescriptor = StreamConfigurationHelper.mkBarMessageDescriptor(
                    null,
                    "NYMEX",
                    840,
                    FloatDataType.ENCODING_FIXED_DOUBLE,
                    FloatDataType.ENCODING_FIXED_DOUBLE
            );

            // create sub-class
            RecordClassDescriptor extended = new RecordClassDescriptor("ExBarMessage", "Extended Bar", false,
                    barMessageDescriptor,
                    new NonStaticDataField("accepted", "Accepted", new BooleanDataType(true)),
                    new NonStaticDataField("ratio", "Ratio", new FloatDataType (FloatDataType.ENCODING_FIXED_DOUBLE, true))
            );


            stream = db.createStream (
                            STREAM_KEY,
                            StreamOptions.fixedType(StreamScope.DURABLE, STREAM_KEY, "Extended Bars", 0, extended)
                    );
        }

        return stream;
    }
}
