package deltix.qsrv.hf.tickdb.pub.topic.exception;

import deltix.data.stream.UnknownChannelException;

/**
 * Thrown if topic with specified key does not exist (wasn't created or already deleted).
 *
 * @author Alexei Osipov
 */
public class TopicNotFoundException extends UnknownChannelException implements TopicApiException {

    public TopicNotFoundException(String message) {
        super(message);
    }
}
