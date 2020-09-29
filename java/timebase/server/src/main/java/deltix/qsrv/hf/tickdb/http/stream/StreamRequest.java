package deltix.qsrv.hf.tickdb.http.stream;

import deltix.qsrv.hf.tickdb.http.XmlRequest;

import javax.xml.bind.annotation.XmlElement;

public abstract class StreamRequest extends XmlRequest {

    /*
        Stream key.
     */
    @XmlElement(name = "stream")
    public String stream;

    /*
        Stream lock id, if any exists.
     */
    @XmlElement(name = "token")
    public String token;
}
