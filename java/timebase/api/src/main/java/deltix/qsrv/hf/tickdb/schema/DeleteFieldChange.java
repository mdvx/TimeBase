package deltix.qsrv.hf.tickdb.schema;

import deltix.qsrv.hf.pub.md.DataField;
import deltix.qsrv.hf.pub.md.StaticDataField;
import deltix.qsrv.hf.pub.md.NonStaticDataField;

import javax.xml.bind.annotation.XmlElement;

public class DeleteFieldChange extends AbstractFieldChange {

    @XmlElement
    private boolean hasImpact = false;

    protected DeleteFieldChange() { } // for jaxb

    public DeleteFieldChange(NonStaticDataField field) {
        this(field, true);        
    }

    public DeleteFieldChange(DataField field) {
        this(field, true);
    }

    private DeleteFieldChange(DataField field, boolean hasImpact) {
        super(field, null);
        this.hasImpact = hasImpact;
    }

    public Impact getChangeImpact() {
        if (source instanceof StaticDataField)
            return Impact.None;
        
        return hasImpact ? Impact.DataConvert : Impact.None;
    }

    public boolean hasErrors() {
        return false;
    }
}
