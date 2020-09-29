package deltix.samples.timebase.process.pub;

/**
 *
 */
public interface MessageProcessorFactory {
    /**
     *  Called to create a new message processor instance. Calls to this method
     *  are not synchronized externally.
     * 
     *  @return     A new processor.
     */
    public MessageProcessor         newProcessor ();
}
