package deltix.qsrv.hf.tickdb.http.stream;


import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.http.IdentityKeyListAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "clearStream")
public class ClearRequest extends StreamRequest {

    @XmlElement()
    @XmlJavaTypeAdapter(IdentityKeyListAdapter.class)
    public IdentityKey[]    identities;
}
