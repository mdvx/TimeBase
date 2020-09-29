package deltix.qsrv.hf.pub.codec;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.util.lang.Factory;

/**
 *
 */
abstract class PolyCodecFactoryBase <C, F>
    implements Factory <C>
{
    protected final Factory <F> []          fixedFactories;

    public PolyCodecFactoryBase (Factory <F> [] fixedFactories) {
        this.fixedFactories = fixedFactories;
    }
}
