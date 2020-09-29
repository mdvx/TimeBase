package deltix.qsrv.hf.tickdb.impl.topic.topicregistry;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.tickdb.pub.Messages;

/**
 * @author Alexei Osipov
 */
public class StubData {
    public static RecordClassDescriptor makeErrorMessageDescriptor ()
    {
        return Messages.ERROR_MESSAGE_DESCRIPTOR;
    }
}
