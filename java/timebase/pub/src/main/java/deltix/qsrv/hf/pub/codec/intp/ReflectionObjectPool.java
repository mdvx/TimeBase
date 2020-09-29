package deltix.qsrv.hf.pub.codec.intp;

import deltix.qsrv.hf.codec.cg.ObjectPool;
import deltix.util.lang.Util;

/**
 *
 */
public class ReflectionObjectPool extends ObjectPool<Object> {
    private final Class<?> clazz;

    public ReflectionObjectPool(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object newItem() {
        return Util.newInstanceNoX(clazz);
    }
}
