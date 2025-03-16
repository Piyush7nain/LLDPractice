package ThreadSafeCounter;

import org.w3c.dom.css.Counter;

public class SynchronizedCounter implements ThreadSafeCounter {
    private int counter = 0;
    public SynchronizedCounter(int initialValue) {
        counter = initialValue;
    }
    @Override
    public void increment() {
        synchronized (this) {
            counter++;
        }
    }

    @Override
    public void decrement() {
        synchronized (this) {
            counter--;
        }
    }

    @Override
    public int get() {
        int value;
        synchronized (this) {
            value = counter;
        }
        return value;
    }
}
