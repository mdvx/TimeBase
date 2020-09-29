package deltix.qsrv.hf.tickdb.pub.task;

import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.pub.IdentityKeyListAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class DataFilter {

    @XmlElement
    @XmlJavaTypeAdapter(IdentityKeyListAdapter.class)
    public IdentityKey[]     instruments;

    @XmlElement
    public String[]                 types;

    @XmlElement
    public long[]                   range;
}
