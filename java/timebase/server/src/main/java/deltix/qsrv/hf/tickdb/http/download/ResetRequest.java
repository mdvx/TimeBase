package deltix.qsrv.hf.tickdb.http.download;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.tickdb.http.IdentityKeyListAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "resetRequest")
public class ResetRequest extends CursorRequest {

    public ResetRequest() {
    }

    public ResetRequest(long id) {
        this.id = id;
    }
}
