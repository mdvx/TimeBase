package deltix.qsrv.hf.tickdb.http;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 */
public class XmlRequest {
    @XmlAttribute()
    public short    version = HTTPProtocol.VERSION;

}
