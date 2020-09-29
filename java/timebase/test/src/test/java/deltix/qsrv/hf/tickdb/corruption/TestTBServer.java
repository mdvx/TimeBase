package deltix.qsrv.hf.tickdb.corruption;

import deltix.qsrv.comm.cat.TBServerCmd;
import deltix.qsrv.config.QuantServiceConfig;
import deltix.qsrv.hf.tickdb.http.AbstractHandler;
import deltix.qsrv.util.log.ServerLoggingConfigurer;
//import deltix.util.spring.SpringUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestTBServer extends TBServerCmd {

    public static void main(String... args) throws Throwable {
        new TestTBServer(args).start();
    }

    private List<LoaderThread> loaders = new ArrayList<>();

    public TestTBServer(String... args) throws Exception {
        super(args);
    }

    @Override
    public void start() {
        super.start();
    }

    public void stop() {
        closeLoaders();
        runner.close();
    }

    private void closeLoaders() {
        loaders.forEach(LoaderThread::close);
        loaders.forEach(l -> {
            try {
                l.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        loaders.clear();

        System.out.println(LocalDateTime.now() + ": " + "All loaders closed");
    }

    @Override
    protected void run() throws Throwable {
        Runtime.getRuntime().addShutdownHook(new TestTBServer.QuantServerShutdownHook());

        try {
            QuantServiceConfig config = this.runner.getConfig().tb;
            config.setProperty("fileSystem.maxFolderSize", String.valueOf(10));
            config.setProperty("fileSystem.maxFileSize", String.valueOf(1024 * 1024));

            this.runner.run();

            if (Test_Corruption.DIRECT_LOADERS) {
                loaders.addAll(Utils.createLoaders(AbstractHandler.TDB, Test_Corruption.LOADERS_COUNT));
            }
            loaders.forEach(Thread::start);

            this.runner.waitForStop();
            System.out.println(LocalDateTime.now() + ": " + "Test TB thread stopped");
        } catch (Throwable ex) {
            LogKeeper.LOG.fatal(ex.toString());
            System.exit(-1);
        }
    }

    private class QuantServerShutdownHook extends Thread {
        private QuantServerShutdownHook() {
        }

        public void run() {
            System.out.println(LocalDateTime.now() + ": " + "Shutting QuantServer down...");
            closeLoaders();
            runner.close();

            try {
                File heapDump = File.createTempFile("dump", "heap");
                File threadDump = File.createTempFile("dump", "thread");
                heapDump.delete();
                threadDump.delete();
                Utils.dumpHeap(heapDump.getAbsolutePath());
                Utils.dumpThreads(threadDump.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(LocalDateTime.now() + ": " + "Server was shut down.");
            ServerLoggingConfigurer.unconfigure();
        }
    }
}