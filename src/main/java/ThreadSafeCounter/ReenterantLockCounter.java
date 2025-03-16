package ThreadSafeCounter;

import java.util.concurrent.locks.ReentrantLock;

public class ReenterantLockCounter implements ThreadSafeCounter {
    private ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    public ReenterantLockCounter(int initialValue) {
        this.count = initialValue;
    }

    @Override
    public void increment() {
        lock.lock();
        try{
            int initialValue = count;
            count++;
            System.out.println("Lock acquired by " + Thread.currentThread().getName()+", increasing count from "+initialValue+" to :"+count);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void decrement() {
        lock.lock();
        try{
            int initialValue = count;
            count--;
            System.out.println("Lock acquired by " + Thread.currentThread().getName()+", decreasing count from "+initialValue+" to :"+count);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int get() {
        int value;
        lock.lock();
        try{
            value = count;
        }finally {
            lock.unlock();
        }
        return value;
    }
}
