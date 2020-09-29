package deltix.qsrv.hf.tickdb.http.stream;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.http.IdentityKeyListAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "truncateStream")
public class TruncateRequest extends StreamRequest {

    @XmlElement()
    public long             time;

    @XmlElement()
    @XmlJavaTypeAdapter(IdentityKeyListAdapter.class)
    public IdentityKey[]    identities;
}
