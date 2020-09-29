package deltix.samples.timebase.process.pub;

import deltix.timebase.api.messages.InstrumentMessage;

/**
 *  Creates message processors that do nothing. Useful for testing 
 *  the framework, or performance measurements.
 */
public class DummyMessageProcessorFactory implements MessageProcessorFactory {
    public static final DummyMessageProcessorFactory    INSTANCE = 
        new DummyMessageProcessorFactory ();
    
    private DummyMessageProcessorFactory () {
    }

    @Override
    public MessageProcessor     newProcessor () {
        return new AbstractMessageProcessor () {
            @Override
            public boolean          process (InstrumentMessage msg) {
                return (true);
            }
        };
    }        
}
