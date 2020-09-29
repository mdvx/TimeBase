package deltix.qsrv.hf.tickdb.pub.query;

import deltix.timebase.messages.IdentityKey;

import java.util.Collection;

public interface EntitySubscriptionChangeListener {

    void entitiesAdded(Collection<IdentityKey> entities);

    void entitiesRemoved(Collection<IdentityKey> entities);

    void allEntitiesAdded();

    void allEntitiesRemoved();
}