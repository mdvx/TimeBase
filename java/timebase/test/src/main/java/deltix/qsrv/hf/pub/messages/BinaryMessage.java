package deltix.qsrv.hf.pub.messages;

import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.Introspector;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.timebase.messages.SchemaElement;
import deltix.timebase.messages.SchemaType;
import deltix.util.collections.generated.ByteArrayList;

/**
 * @author Daniil Yarmalkevich
 * Date: 11/12/2019
 */
public class BinaryMessage extends InstrumentMessage {

    private static final Introspector ix =
            Introspector.createEmptyMessageIntrospector();
    private static RecordClassDescriptor myDescriptor = null;

    @SchemaElement(
            title = "Nullable BINARY"
    )
    public ByteArrayList binary_n;

    @SchemaElement(
            title = "Non-nullable CHAR"
    )
    @SchemaType(
            isNullable = false
    )
    public char char_c;

    @SchemaElement(
            title = "Nullable CHAR"
    )
    public char char_n;

    public static synchronized RecordClassDescriptor getClassDescriptor() {
        if (myDescriptor == null) {
            try {
                myDescriptor = ix.introspectRecordClass(BinaryMessage.class);
            } catch (Introspector.IntrospectionException x) {
                throw new RuntimeException(x);   // Unexpected, this should be reliable.
            }
        }

        return (myDescriptor);
    }

}
