package deltix.qsrv.hf.tickdb.corruption;

public interface MessageLoader {

    void update(long timestamp);

    long send();

    void close();

}
