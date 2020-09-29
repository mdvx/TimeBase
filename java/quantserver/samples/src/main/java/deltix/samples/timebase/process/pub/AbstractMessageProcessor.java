package deltix.samples.timebase.process.pub;

/**
 *  Implements all optional methods of {@link MessageProcessor}.
 */
public abstract class AbstractMessageProcessor implements MessageProcessor {
    @Override
    public void         init (ProcessingJob job) {
    }

    @Override
    public void         close () {
    }   
}
