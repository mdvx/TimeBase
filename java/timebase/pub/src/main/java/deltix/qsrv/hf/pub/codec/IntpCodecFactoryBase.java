package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.qsrv.hf.pub.TypeLoader;
import deltix.util.lang.Factory;

/**
 *
 */
abstract class IntpCodecFactoryBase <C>
    implements Factory <C>
{
    protected final RecordLayout    layout;

    public IntpCodecFactoryBase (RecordClassDescriptor cd) {
        this.layout = new RecordLayout (cd);
    }

    public IntpCodecFactoryBase (
        TypeLoader              loader,
        RecordClassDescriptor   cd
    )
    {
        this.layout = new RecordLayout (loader, cd);
    }
}
