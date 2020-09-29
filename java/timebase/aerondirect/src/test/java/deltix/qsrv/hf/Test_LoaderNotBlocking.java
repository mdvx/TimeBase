package deltix.qsrv.hf;

import deltix.qsrv.hf.tickdb.pub.Messages;
import deltix.streaming.MessageChannel;

import deltix.timebase.messages.InstrumentMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.topic.loader.DirectLoaderFactory;
import deltix.timebase.messages.service.ErrorMessage;
import io.aeron.CommonContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Alexei Osipov
 */
@Category(Object.class)
public class Test_LoaderNotBlocking extends BaseAeronTest {

    /**
     * Tests that message sending does not block when there are no consumers.
     */
    @Test(timeout = 30_000)
    public void testMissingConsumer() {
        String channel = CommonContext.IPC_CHANNEL;
        int dataStreamId = new Random().nextInt();
        int serverMetadataStreamId = dataStreamId + 1;
        List<RecordClassDescriptor> types = Collections.singletonList(Messages.ERROR_MESSAGE_DESCRIPTOR);
        byte loaderNumber = 1;

        MessageChannel<InstrumentMessage> messageChannel = new DirectLoaderFactory().create(aeron, false, channel, channel, dataStreamId, serverMetadataStreamId, types, loaderNumber, new ByteArrayOutputStream(8 * 1024), Collections.emptyList(), null, null);

        ErrorMessage msg = new ErrorMessage();
        msg.setSymbol("ABC");
        msg.setMessageText("Not exists");

        for (int i = 0; i < 1_000_000; i++) {
            messageChannel.send(msg);
        }
        messageChannel.close();
    }
}