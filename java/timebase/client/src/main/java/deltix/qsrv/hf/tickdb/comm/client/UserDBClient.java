package deltix.qsrv.hf.tickdb.comm.client;

import deltix.data.stream.DXChannel;
import deltix.qsrv.hf.pub.ChannelCompression;
import deltix.qsrv.hf.pub.ChannelQualityOfService;
import deltix.timebase.messages.IdentityKey;
import deltix.qsrv.hf.pub.codec.CodecFactory;
import deltix.qsrv.hf.pub.md.MetaData;
import deltix.qsrv.hf.tickdb.pub.*;
import deltix.qsrv.hf.tickdb.pub.query.InstrumentMessageSource;
import deltix.qsrv.hf.tickdb.pub.query.Parameter;
import deltix.qsrv.hf.tickdb.pub.topic.TopicDB;
import deltix.thread.affinity.AffinityConfig;
import deltix.util.concurrent.QuickExecutor;
import deltix.util.lang.GrowthPolicy;
import deltix.util.parsers.CompilationException;
import deltix.util.parsers.Element;
import deltix.util.vsocket.VSChannel;
import java.io.File;
import java.io.IOException;
import java.security.Principal;

public class UserDBClient implements DXRemoteDB {

    private final TickDBClient delegate;
    private final Principal user;

    public UserDBClient(TickDBClient delegate, Principal user) {
        this.delegate = delegate;
        this.user = user;
    }

    @Override
    public SessionClient getSession() {
        return delegate.getSession();
    }

    @Override
    public QuickExecutor getQuickExecutor() {
        return delegate.getQuickExecutor();
    }

    /// Shared

    @Override
    public VSChannel connect() throws IOException {
        final Principal prevUser = UserContext.set(user);
        try {
            return delegate.connect();
        } finally {
            UserContext.set(prevUser);
        }
    }

    @Override
    public VSChannel connect(ChannelType type, boolean autoCommit, boolean noDelay, ChannelCompression c, int channelBufferSize) throws IOException {
        final Principal prevUser = UserContext.set(user);
        try {
            return delegate.connect(type, autoCommit, noDelay, c, channelBufferSize);
        } finally {
            UserContext.set(prevUser);
        }
    }

    @Override
    public int getServerProtocolVersion() {
        return delegate.getServerProtocolVersion();
    }

    @Override
    public CodecFactory getCodecFactory(ChannelQualityOfService channelQOS) {
        return delegate.getCodecFactory(channelQOS);
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    /// User-sensitive

    @Override
    public DXTickStream         getStream(String key) {
        TickStreamClient stream = delegate.getStream(key);
        return (stream != null) ? new TickStreamClient(this, stream) : null;
    }

    @Override
    public DXTickStream[]       listStreams() {
        TickStreamClient[] streams = delegate.listStreams();

        TickStreamClient[] wrapped = new TickStreamClient[streams.length];
        for (int i = 0; i < wrapped.length; i++)
            wrapped[i] = new TickStreamClient(this, streams[i]);

        return wrapped;
    }

    @Override
    public DXChannel[] listChannels() {
        return new DXChannel[0];
    }

    @Override
    public DXTickStream createAnonymousStream(StreamOptions options) {
        return delegate.createAnonymousStream(options);
    }

    @Override
    public long         getServerTime() {
        return delegate.getServerTime();
    }

    @Override
    public DXTickStream createStream(String key, StreamOptions options) {
        final Principal prevUser = UserContext.set(user);
        try {
            return new TickStreamClient(this, delegate.createStream(key, options));
        } finally {
            UserContext.set(prevUser);
        }
    }

    @Override
    public DXTickStream createFileStream(String key, String dataFile) {
        final Principal prevUser = UserContext.set(user);
        try {
            return new TickStreamClient(this, delegate.createFileStream(key, dataFile));

        } finally {
            UserContext.set(prevUser);
        }
    }

    @Override
    public DXTickStream createStream(String key, String name, String description, int distributionFactor) {
        final Principal prevUser = UserContext.set(user);
        try {
            return new TickStreamClient(this, delegate.createStream(key, name, description, distributionFactor));
        } finally {
            UserContext.set(prevUser);
        }
    }

    @Override
    public long getMetaDataVersion() {
        return delegate.getMetaDataVersion();
    }

    @Override
    public MetaData getMetaData() {
        return delegate.getMetaData();
    }

    public InstrumentMessageSource          executeQuery (
            String                                  qql,
            Parameter ...                           params
    )
            throws CompilationException
    {
        return (executeQuery (qql, null, null, params));
    }

    @Override
    public InstrumentMessageSource          executeQuery (
            String                                  qql,
            SelectionOptions                        options,
            Parameter ...                           params
    )
            throws CompilationException
    {
        return (executeQuery (qql, options, null, params));
    }

    @Override
    public InstrumentMessageSource          executeQuery (
            String                                  qql,
            SelectionOptions                        options,
            CharSequence []                         ids,
            Parameter ...                           params
    )
            throws CompilationException
    {
        return (executeQuery (qql, options, null, ids, Long.MIN_VALUE, params));
    }

    @Override
    public InstrumentMessageSource          executeQuery (
            Element                                 qql,
            SelectionOptions                        options,
            TickStream []                           streams,
            CharSequence []                         ids,
            long                                    time,
            Parameter ...                           params
    )
            throws CompilationException
    {
        return (executeQuery (qql.toString (), options, streams, ids, time, params));
    }

    public InstrumentMessageSource          executeQuery (
            String                                  qql,
            SelectionOptions                        options,
            TickStream []                           streams,
            CharSequence []                         ids,
            long                                    time,
            Parameter ...                           params
    )
            throws CompilationException
    {
        return TickCursorClientFactory.create(this, options, time, qql, params, ids, null, getAeronContext(), streams);
    }

    private void                            assertOpen() {
        if (!isOpen ())
            throw new IllegalStateException ("Database is not open");
    }

    @Override
    public TickCursor           createCursor (
            SelectionOptions            options,
            TickStream ...              streams
    )
    {
        assertOpen();

        long time = options != null && options.reversed ? Long.MIN_VALUE : Long.MAX_VALUE;
        return TickCursorClientFactory.create(this, options, time, null, null, new IdentityKey[0], null, getAeronContext(), streams);
    }

    @Override
    public TickCursor select(long time, SelectionOptions options, String[] types, CharSequence[] symbols, TickStream... streams) {
        assertOpen();

        if (streams != null && streams.length == 1)
            return streams[0].select(time, options, types, symbols);

        return TickCursorClientFactory.create(this, options, time, null, null, symbols, types, getAeronContext(), streams);
    }

    @Override
    public TickCursor select(long time, SelectionOptions options, String[] types, IdentityKey[] ids, TickStream... streams) {
        assertOpen();

        if (streams != null && streams.length == 1)
            return streams[0].select(time, options, types, ids);

        return TickCursorClientFactory.create(this, options, time, null, null, ids, types, getAeronContext(), streams);
    }

    @Override
    public TickCursor select(long time, SelectionOptions options, String[] types, TickStream... streams) {
        assertOpen();

        if (streams != null && streams.length == 1)
            return streams[0].select(time, options, types);

        return TickCursorClientFactory.create(this, options, time, null, null, types, getAeronContext(), streams);
    }



    @Override
    public TickCursor select(long time, SelectionOptions options, TickStream... streams) {
        assertOpen();

        if (streams != null && streams.length == 1)
            return streams[0].select(time, options);

        return TickCursorClientFactory.create(this, options, time, null, null, getAeronContext(), streams);
    }

    @Override
    public void setAffinityConfig(AffinityConfig affinityConfig) {
        delegate.setAffinityConfig(affinityConfig);
    }

    /// Unsupported

    @Override
    public void open(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void warmUp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void coolDown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void format() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trimToSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSizeOnDisk() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File[] getDbDirs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGrowthPolicy(GrowthPolicy policy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TopicDB getTopicDB() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTopicDBSupported() {
        return false;
    }

    /// Helpers

    DXClientAeronContext getAeronContext() {
        return delegate.getAeronContext();
    }
}
