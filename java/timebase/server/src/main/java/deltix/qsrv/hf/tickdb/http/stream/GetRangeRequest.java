package deltix.qsrv.hf.tickdb.http.stream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.http.IdentityKeyListAdapter;

/**
 *
 */
@XmlRootElement(name = "getTimeRange")
public class GetRangeRequest extends StreamRequest {

    @XmlElement()
    @XmlJavaTypeAdapter(IdentityKeyListAdapter.class)
    public IdentityKey[]    identities;
}
