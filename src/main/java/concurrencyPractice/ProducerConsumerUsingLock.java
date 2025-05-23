package concurrencyPractice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerUsingLock {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    resource.produce(i);
                    Thread.sleep(1000); // Simulate time to produce
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    resource.consume();
                    Thread.sleep(1500); // Simulate time to consume
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();
        consumer.start();
    }
}

class SharedResource {
    private int data;
    private boolean available = false; // Tracks if data is ready
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    // Producer method
    public void produce(int value) throws InterruptedException {
        lock.lock();
        try {
            while (available) {
                condition.await(); // Wait until data is consumed
            }
            data = value;
            System.out.println("Produced: " + data);
            available = true;
            condition.signal(); // Notify the consumer
        } finally {
            lock.unlock();
        }
    }

    // Consumer method
    public void consume() throws InterruptedException {
        lock.lock();
        try {
            while (!available) {
                condition.await(); // Wait until data is produced
            }
            System.out.println("Consumed: " + data);
            available = false;
            condition.signal(); // Notify the producer
        } finally {
            lock.unlock();
        }
    }
}