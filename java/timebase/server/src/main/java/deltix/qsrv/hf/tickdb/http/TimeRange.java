package deltix.qsrv.hf.tickdb.http;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 */
@XmlType()
public class TimeRange {
    @XmlElement()
    long from;

    @XmlElement()
    long to;

    // JAXB
    TimeRange() {
    }

    public TimeRange(long from, long to) {
        this.from = from;
        this.to = to;
    }
}
