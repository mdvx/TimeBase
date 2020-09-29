package deltix.qsrv.hf.tickdb.http;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 */
@XmlType(name = "recordType")
public class RecordType {
    @XmlElement()
    String name;

    @XmlElement(name = "column")
    Column[] columns;
}
