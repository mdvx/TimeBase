package deltix.qsrv.hf.tickdb.corruption;

import java.util.ArrayList;
import java.util.List;

public class CompositeLoader implements MessageLoader {

    private final List<MessageLoader> loaders = new ArrayList<>();

    public CompositeLoader(List<MessageLoader> loaders) {
        this.loaders.addAll(loaders);
    }

    @Override
    public void update(long timestamp) {
        loaders.forEach(loader -> loader.update(timestamp));
    }

    @Override
    public long send() {
        return loaders.stream().mapToLong(MessageLoader::send).sum();
    }

    @Override
    public void close() {
        loaders.forEach(MessageLoader::close);
    }
}
