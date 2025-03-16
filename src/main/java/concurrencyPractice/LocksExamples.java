package concurrencyPractice;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class LocksExamples {
    public static void main(String[] args) throws InterruptedException {
        MyLock myLock = new AtomicLock();
        Thread t1 = new Thread(() ->{
            for (int i = 0; i < 10; i++) {
                myLock.action();
            }
        });
        Thread t2 = new Thread(() ->{
            for (int i = 0; i < 10; i++) {
                myLock.action();
            }
        });
        t2.start();
//        Thread.sleep(100);
        t1.start();
//        Thread.sleep(1000);
//        t1.interrupt();

        t1.join();
    }
}
interface MyLock{
    void action();

}

class AtomicLock implements MyLock{
    private AtomicInteger atomicInteger = new AtomicInteger();
    private int count = 0;
    public void action(){
        count++;
//        atomicInteger.incrementAndGet();
        System.out.println(" Update from Thread : " + Thread.currentThread().getName() + " Integer : " +0 +", Count: "+ count);
    }

}

// The thread waiting for lock can be interrupted while it waits for the lock
class InterruptLock implements MyLock{
    private final ReentrantLock lock = new ReentrantLock();

    public void action() {
        try {
            lock.lockInterruptibly();
            System.out.println(Thread.currentThread().getName() + " is locked");
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " is interrupted");
        } finally {
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println(Thread.currentThread().getName() + " is unlocked");
            }
        }
    }
}
class MyTryLock implements MyLock{
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    public void action() {
        if(lock.tryLock()) {
            try {
                count++;
                System.out.println("Lock acquired by : "+Thread.currentThread().getName() + ": count :" + count);
            } finally {
                lock.unlock();
            }
        }else{
            System.out.println("Lock not acquired by : "+Thread.currentThread().getName());
        }
    }
}
class JustLock implements MyLock{
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    public void action() {
        lock.lock();
        try{
            count++;
            System.out.println("Thread " + Thread.currentThread().getName() + " is " + getCount());
        } finally {
            lock.unlock();
        }

    }
    public int getCount() {
        return count;
    }
}