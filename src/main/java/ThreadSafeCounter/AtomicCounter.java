package ThreadSafeCounter;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter implements ThreadSafeCounter {
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    public AtomicCounter(int initialValue) {
        atomicInteger.set(initialValue);
    }

    @Override
    public void increment() {
        atomicInteger.incrementAndGet();
    }

    @Override
    public void decrement() {
        atomicInteger.decrementAndGet();
    }

    @Override
    public int get() {
        return atomicInteger.get();
    }
}
