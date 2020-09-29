package deltix.qsrv.hf.tickdb.impl.topic;

import deltix.qsrv.QSHome;
import deltix.qsrv.hf.tickdb.comm.server.aeron.DXServerAeronContext;
import deltix.qsrv.hf.tickdb.impl.topic.topicregistry.DirectTopicRegistry;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexei Osipov
 */
@ParametersAreNonnullByDefault
public class TopicRegistryFactory {
    public static DirectTopicRegistry initRegistryAtQSHome(DXServerAeronContext aeronContext) {
        return initRegistryAtPath(aeronContext, QSHome.get());
    }

    public static DirectTopicRegistry initRegistryAtPath(DXServerAeronContext aeronContext, String path) {
        TopicStorage topicStorage = TopicStorage.createAtPath(path);
        DirectTopicRegistry topicRegistry = new DirectTopicRegistry(topicStorage.getPersistingListener());
        topicStorage.loadTopicDataInto(topicRegistry, aeronContext.getStreamIdGenerator());
        return topicRegistry;
    }
}
