package deltix.qsrv.hf.tickdb.corruption;

public class DeterminedLoader {

    private final MessageLoader loader;

    private long startTime;
    private double currentTime;
    private double timeIncrement;

    private long totalLoad = 0;

    DeterminedLoader(MessageLoader loader, long startTime, int messagesPerSecond) {
        this.loader = loader;
        this.startTime = startTime;
        this.timeIncrement = 1000.0d / (double) messagesPerSecond;
    }

    public long load(int count) {
        for (int i = 0; i < count; ++i) {
            loader.update(startTime + (long) (currentTime));
            currentTime += timeIncrement;
            totalLoad += loader.send();
        }

        return startTime + (long) (currentTime);
    }

    public void close() {
        System.out.println("Total load: " + totalLoad);
        loader.close();
    }

}
