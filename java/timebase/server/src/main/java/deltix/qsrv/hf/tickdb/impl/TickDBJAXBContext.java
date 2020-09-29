package deltix.qsrv.hf.tickdb.impl;

import deltix.gflog.Log;
import deltix.gflog.LogFactory;
import deltix.util.lang.Depends;
import deltix.util.xml.JAXBContextFactory;
import deltix.util.xml.SkipValidationEventHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 */
@Depends ({ "../jaxb.index", "../../pub/jaxb.index" })
//@DependsOnClass (UHFJAXBContext.class)
public class TickDBJAXBContext {
    private static final Log LOG = LogFactory.getLog(TickDBJAXBContext.class);
    public static final JAXBContext       INSTANCE;
    
    static {
        try {
        	INSTANCE = create ();
        } catch (JAXBException x) {
            LOG.error("Failed to initialize JAXB context for TimeBase: %s").with(x);
            throw new ExceptionInInitializerError (x);
        }
    }    
    
    public static Unmarshaller createUnmarshaller ()
        throws JAXBException
    {
        Unmarshaller unmarshaller = INSTANCE.createUnmarshaller();
        unmarshaller.setEventHandler(new SkipValidationEventHandler(LOG));
        return unmarshaller;
    }

    public static Marshaller        createMarshaller ()
        throws JAXBException
    {
        return (JAXBContextFactory.createStdMarshaller (INSTANCE));
    }
    
    private static JAXBContext      create () 
    	throws JAXBException
    {
        return JAXBContextFactory.newInstance (
            "deltix.qsrv.hf.tickdb.pub:" + "deltix.qsrv.hf.tickdb.impl:" + "deltix.qsrv.hf.pub.md"
        );            
    }
}
