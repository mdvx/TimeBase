package deltix.qsrv.hf.tickdb.pub;

import deltix.streaming.MessageChannel;
import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.tickdb.pub.query.SubscriptionChangeListener;
import deltix.timebase.messages.MessageInfo;

import java.io.Flushable;

/**
 *
 */
public interface TickLoader<T extends MessageInfo> extends MessageChannel<T>, Flushable {

    public WritableTickStream   getTargetStream ();

    public void         addEventListener (LoadingErrorListener listener);

    public void         removeEventListener (LoadingErrorListener listener);

    public void         addSubscriptionListener (SubscriptionChangeListener listener);

    public void         removeSubscriptionListener (SubscriptionChangeListener listener);

    public void         removeUnique(T msg);
}
