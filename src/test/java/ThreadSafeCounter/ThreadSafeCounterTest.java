package ThreadSafeCounter;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class ThreadSafeCounterTest {

    @Test
    public void testCounter_synchronizedCounter() {
        ThreadSafeCounter counter = new SynchronizedCounter(0);
        counter.increment();
        assertEquals(1, counter.get());
        counter.decrement();
        assertEquals(0, counter.get());
    }

    @Test
    public void testCounterMultiThreaded() throws InterruptedException {
        final int threadCount = 100;
        final int incrementsPerThread = 1000;
        ThreadSafeCounter counter = new SynchronizedCounter(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(threadCount * incrementsPerThread, counter.get());
    }

    @Test
    public void testCounterWithMixedOperations() throws InterruptedException {
        final int threadCount = 100;
        ThreadSafeCounter counter = new SynchronizedCounter(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 500; j++) {
                    counter.increment();
                }
                for (int j = 0; j < 500; j++) {
                    counter.decrement();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(0, counter.get());
    }

    @Test
    public void testCounter_lockCounter() {
        ThreadSafeCounter counter = new ReenterantLockCounter(0);
        counter.increment();
        assertEquals(1, counter.get());
        counter.decrement();
        assertEquals(0, counter.get());
    }

    @Test
    public void testCounterMultiThreaded_lockCounter() throws InterruptedException {
        final int threadCount = 10;
        final int incrementsPerThread = 10;
        ThreadSafeCounter counter = new ReenterantLockCounter(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(threadCount * incrementsPerThread, counter.get());
    }

    @Test
    public void testCounterWithMixedOperations_lockCounter() throws InterruptedException {
        final int threadCount = 100;
        ThreadSafeCounter counter = new ReenterantLockCounter(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 500; j++) {
                    counter.increment();
                }
                for (int j = 0; j < 500; j++) {
                    counter.decrement();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(0, counter.get());
    }

    @Test
    public void testCounter_AtomicCounter() {
        ThreadSafeCounter counter = new AtomicCounter(0);
        counter.increment();
        assertEquals(1, counter.get());
        counter.decrement();
        assertEquals(0, counter.get());
    }

    @Test
    public void testCounterMultiThreaded_AtomicCounter() throws InterruptedException {
        final int threadCount = 10;
        final int incrementsPerThread = 10;
        ThreadSafeCounter counter = new AtomicCounter(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(threadCount * incrementsPerThread, counter.get());
    }

    @Test
    public void testCounterWithMixedOperations_AtomicCounter() throws InterruptedException {
        final int threadCount = 100;
        ThreadSafeCounter counter = new AtomicCounter(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 500; j++) {
                    counter.increment();
                }
                for (int j = 0; j < 500; j++) {
                    counter.decrement();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(0, counter.get());
    }

}
