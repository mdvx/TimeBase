package deltix.qsrv.hf.tickdb.comm.client;

import deltix.qsrv.hf.pub.ChannelCompression;
import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.tickdb.pub.DXTickDB;
import deltix.thread.affinity.AffinityConfig;
import deltix.util.concurrent.QuickExecutor;
import deltix.util.vsocket.VSChannel;

import java.io.IOException;

interface DXRemoteDB extends DXTickDB {

    VSChannel           connect() throws IOException;

    /**
     * @param channelBufferSize Sets channel buffer size. If value is 0 then buffer size determined automatically.
     */
    VSChannel           connect(ChannelType type, boolean autoCommit, boolean noDelay, ChannelCompression c, int channelBufferSize) throws IOException;

    int                 getServerProtocolVersion();

    CodecFactory        getCodecFactory(ChannelQualityOfService channelQOS);

    SessionClient       getSession(); // TODO: refactor to interface

    /**
     * Sets CPU affinity for TB client threads, if needed.
     */
    void                setAffinityConfig(AffinityConfig affinityConfig);

    QuickExecutor       getQuickExecutor();
}
