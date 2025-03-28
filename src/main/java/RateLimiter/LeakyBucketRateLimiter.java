package RateLimiter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LeakyBucketRateLimiter implements RateLimiter {
    private final int size;
    private final ConcurrentLinkedDeque<Runnable> requestStore = new ConcurrentLinkedDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    public LeakyBucketRateLimiter(int size, long rate) {
        this.size = size;
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        service.scheduleAtFixedRate(() -> {
            try {
                executeRequests();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, rate, rate, TimeUnit.MILLISECONDS);
    }
    @Override
    public boolean allowRequest() {
        try{
            lock.lock();
            if(requestStore.size() >= size){
                System.out.println("Thread: " + Thread.currentThread().getName() + " Queue full. Rejecting Task-"+taskCounter.incrementAndGet()+" Size " + requestStore.size());
                return false;
            }
            System.out.println("Thread: " + Thread.currentThread().getName() + " Adding new task. Queue size: " + requestStore.size());
            Runnable r = () -> {
                System.out.println("Thread: " + Thread.currentThread().getName() + " Running Task-"+taskCounter.incrementAndGet()+" . Queue size: " + requestStore.size());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            };
            notEmpty.signalAll();
            requestStore.addLast(r);
            return true;
        } finally {
            lock.unlock();
        }
    }
    private void executeRequests() throws InterruptedException {
        try{
            System.out.println("Thread: " + Thread.currentThread().getName() + " Starting Executor ");
            if(lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                while (requestStore.isEmpty()) {
                    System.out.println("Thread: " + Thread.currentThread().getName() + " Queue empty. Waiting for requests...");
                    notEmpty.await();
                }
                System.out.println("Thread: " + Thread.currentThread().getName() + " Started running task. Queue size: " + requestStore.size());
                Runnable r = requestStore.removeFirst();
                r.run();
                System.out.println("Thread: " + Thread.currentThread().getName() + " Finished Executing Task. Queue size: " + requestStore.size());

            } else{
                System.out.println("Thread: " + Thread.currentThread().getName() + " Lock Not acquired. Waiting for requests...");
            }

        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
