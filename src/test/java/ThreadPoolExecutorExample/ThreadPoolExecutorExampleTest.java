package ThreadPoolExecutorExample;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


class ThreadPoolExecutorExampleTest {
    private ThreadPoolExecutorExample threadPool;
    @BeforeEach
    public void setUp() {
        threadPool = new ThreadPoolExecutorExample(2, 4, 10);
    }

    @Test
    public void testExecuteTask() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable task = counter::incrementAndGet;

        threadPool.execute(task);
        Thread.sleep(100); // Give some time for the task to be processed

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    public void testMaxPoolSize() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 2; i++) {
            Runnable task = () -> {
                counter.incrementAndGet();
                try { Thread.sleep(50000); } catch (InterruptedException ignored) {}
            };
            threadPool.execute(task);
//            Thread.sleep(2000);
        }
        Thread.sleep(5000); // Allow tasks to start

        Assertions.assertEquals(2, threadPool.getActiveCount());
    }

    @Test
    public void testTaskQueueOverflow() throws InterruptedException {
        Runnable task = () -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        };

        for (int i = 0; i < 6; i++) { // corePoolSize + maxPoolSize + queueSize
            threadPool.execute(task);
        }

        Thread.sleep(10000);
    }

    @Test
    public void testShutdown() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable task = counter::incrementAndGet;

        threadPool.execute(task);
        threadPool.shutdown();
        Thread.sleep(100); // Give time for shutdown

        Assertions.assertTrue(threadPool.getActiveCount() == 0 || counter.get() == 1);
    }

    @Test
    public void testShutdownNow() throws InterruptedException {
        Runnable task = () -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        };

        threadPool.execute(task);
        threadPool.shutdownNow();
        Thread.sleep(100);

        Assertions.assertEquals(0, threadPool.getQueueLength());
    }
}
