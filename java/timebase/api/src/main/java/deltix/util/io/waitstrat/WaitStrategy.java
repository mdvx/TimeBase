package deltix.util.io.waitstrat;

import deltix.util.lang.Changeable;
import deltix.util.lang.Disposable;
import java.io.IOException;

/**
 *
 */
public interface WaitStrategy extends Disposable {
    public void                 waitSignal() throws InterruptedException;
    public void                 signal();
}
