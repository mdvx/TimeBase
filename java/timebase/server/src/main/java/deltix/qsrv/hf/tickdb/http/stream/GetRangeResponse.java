package deltix.qsrv.hf.tickdb.http.stream;

import deltix.qsrv.hf.tickdb.http.TimeRange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "getTimeRangeResponse")
public class GetRangeResponse {
    @XmlElement()
    public TimeRange timeRange;
}
