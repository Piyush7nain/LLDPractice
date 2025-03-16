package ProducerConsumer;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProducerConsumerTest {
    @Test
    public void testSingleProducerSingleConsumer() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(new Producer(buffer, 1,1));
        executor.submit(new Consumer(buffer, 1,1));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, buffer.size());
    }

    @Test
    public void testMultipleProducersSingleConsumer() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(10);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(new Producer(buffer, 5,5));
        executor.submit(new Producer(buffer, 6,5));
        executor.submit(new Consumer(buffer, 10,10));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, buffer.size());
    }

    @Test
    public void testSingleProducerMultipleConsumers() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(10);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(new Producer(buffer, 10, 10));
        executor.submit(new Consumer(buffer, 5, 5));
        executor.submit(new Consumer(buffer, 5, 5));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, buffer.size());
    }

    @Test
    public void testMultipleProducersMultipleConsumers() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(10);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(new Producer(buffer, 5, 5));
        executor.submit(new Producer(buffer, 5,5));
        executor.submit(new Consumer(buffer, 5,5));
        executor.submit(new Consumer(buffer, 5,5));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, buffer.size());
    }

    @Test
    public void testBufferUnderflowPrevention() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(1);
        Consumer consumer = new Consumer(buffer, 1,1);
        Thread consumerThread = new Thread(consumer);

        consumerThread.start();
        Thread.sleep(100); // Ensure consumer waits on empty buffer
        assertEquals(0, buffer.size());
        consumerThread.interrupt();
    }

    @Test
    public void testBufferOverflowPrevention() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(1);
        Producer producer = new Producer(buffer, 2,2);
        Thread producerThread = new Thread(producer);

        producerThread.start();
        Thread.sleep(100); // Ensure producer waits when buffer is full
        assertEquals(1, buffer.size());
        producerThread.interrupt();
    }
}
