package deltix.qsrv.hf.tickdb.corruption;

import deltix.util.lang.Disposable;

import java.time.LocalDateTime;

public class LoaderThread extends Thread implements Disposable {
    private final long updatesPerSecond;

    private final MessageLoader loader;
    private final long sendsPerUpdate;

    private volatile boolean running = true;

    private long totalLoad = 0;

    public LoaderThread(String name, MessageLoader loader, long sendsPerUpdate) {
        this(name, loader, sendsPerUpdate, 10);
    }

    public LoaderThread(String name, MessageLoader loader, long sendsPerUpdate, int updatesPerSecond) {
        super(name);

        this.loader = loader;
        this.updatesPerSecond = updatesPerSecond;
        this.sendsPerUpdate = sendsPerUpdate;
    }

    @Override
    public void run() {
        System.out.println("Start thread " + getName());

        long messages = 0;

        long lastFrameTime;
        long lastLogTime = System.nanoTime();
        while (running) {
            lastFrameTime = System.nanoTime();

            loader.update(Long.MIN_VALUE);
            for (int i = 0; i < sendsPerUpdate; ++i) {
                messages += loader.send();
            }

            totalLoad += messages;

            long endTime = lastFrameTime + 1_000_000_000 / updatesPerSecond;
            while (System.nanoTime() < endTime) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                }
            }

            // log messages count
            float secondsPassed = (lastFrameTime - lastLogTime) / 1_000_000_000.0f;
            if (secondsPassed > 5.0f) {
                System.out.println(LocalDateTime.now() + ": " + getName() + ": " + (messages / secondsPassed) + " msgs/s");
                messages = 0;
                lastLogTime = System.nanoTime();
            }

        }

        loader.close();
    }

    @Override
    public void close() {
        System.out.println("Total messages: " + totalLoad);
        running = false;
    }
}