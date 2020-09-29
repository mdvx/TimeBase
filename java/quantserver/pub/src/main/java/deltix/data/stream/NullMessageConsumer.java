package deltix.data.stream;

import deltix.streaming.MessageChannel;

/**
 *  Ignores messages
 */
public class NullMessageConsumer <T> implements MessageChannel<T> {
    public void         send (T message) {        
    }

    public void         close () {
    }    
}
