package deltix.qsrv.hf.tickdb.pub.task;

import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.md.RecordClassSet;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.schema.*;
import deltix.timebase.messages.IdentityKey;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;

/**
 * 
 */
@XmlRootElement(name="StreamCopyTask")
public class StreamCopyTask implements TransformationTask {

    @XmlElement
    public CopyStreamMetaDataChange change;

    @XmlElement
    public String[]                 sources;

    @XmlElement
    public String                   target;

    @XmlElement
    public long                     startTime;

    @XmlElement
    public long                     endTime;

    @XmlElement
    public boolean                  isBackground = true;

    @XmlElement
    public String[]                 subscribedEntities;

    @XmlElement
    public String[]                 subscribedTypeNames;

    public StreamCopyTask() { } // JAXB

    public StreamCopyTask(MetaDataChange change) {
        CopyStreamMetaDataChange streamMd = new CopyStreamMetaDataChange(change.getSource(),
                change.getTarget(),
                change.mapping);
        streamMd.changes = change.changes;
        this.change = streamMd;
    }

    public void                 invalidate(DXTickDB db) {

        final SimpleClassSet in = new SimpleClassSet ();
        for (String stream : sources) {
            DXTickStream tickStream = db.getStream(stream);
            in.addContentClasses (DXTickStream.getClassDescriptors(tickStream));
        }

        DXTickStream stream = db.getStream(target);
        
        final RecordClassSet out = new RecordClassSet();
        if (stream.isFixedType ())
            out.addContentClasses (stream.getFixedType ());
        else
            out.addContentClasses (stream.getPolymorphicDescriptors());

        change.invalidate(in, out);
    }

    public DXTickStream[]       getSources(DXTickDB db) {
        
        ArrayList<DXTickStream> list = new ArrayList<DXTickStream>();
        for (int i = 0; i < sources.length; i++)
            list.add(db.getStream(sources[i]));

        return list.toArray(new DXTickStream[list.size()]);
    }

    @Override
    public boolean              isBackground() {
        return isBackground;
    }
}
