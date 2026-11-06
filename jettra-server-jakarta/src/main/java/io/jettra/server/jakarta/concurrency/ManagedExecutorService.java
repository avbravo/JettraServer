package io.jettra.server.jakarta.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Implementación de Jakarta Concurrency 3.0 respaldada por Java 25 Virtual Threads.
 */
public class ManagedExecutorService implements AutoCloseable {

    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ThreadFactory virtualThreadFactory = Thread.ofVirtual().name("jettra-concurrency-", 0).factory();

    public void execute(Runnable command) {
        virtualExecutor.execute(command);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return virtualExecutor.submit(task);
    }

    public Future<?> submit(Runnable task) {
        return virtualExecutor.submit(task);
    }

    public ThreadFactory getManagedThreadFactory() {
        return virtualThreadFactory;
    }

    @Override
    public void close() {
        virtualExecutor.close();
    }
}
