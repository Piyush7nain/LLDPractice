package RateLimiter;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class FixedWindowRateLimiter implements RateLimiter {
    private final int windowSize;
    private final TimeUnit timeUnit;
    private final AtomicInteger count;
    private final int limit;
    private final AtomicLong windowStart;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition awaitCapacity = lock.newCondition();
    private final Queue<CompletableFuture<Boolean>> waitingQueue = new ConcurrentLinkedQueue<>();
    public FixedWindowRateLimiter(int limit, int windowSize, TimeUnit timeUnit) {
        this.limit = limit;
        this.windowSize = windowSize;
        this.timeUnit = timeUnit;
        this.count = new AtomicInteger(0);
        this.windowStart = new AtomicLong(System.currentTimeMillis());
        ScheduledExecutorService refreshService = new ScheduledThreadPoolExecutor(1);
        refreshService.scheduleAtFixedRate(this::refreshCounter,this.windowSize,this.windowSize,this.timeUnit);
    }
    @Override
    public boolean allowRequest() {
       try{
           lock.lock();
           long now = System.currentTimeMillis();
           if(now - windowStart.get() > windowSize){
               refreshCounter();
           }
           if(count.get() <= limit){
               count.incrementAndGet();
               return true;
           }
           CompletableFuture<Boolean> future  = new CompletableFuture<>();
           waitingQueue.add(future);
           while(!future.isDone()){
               awaitCapacity.await();
           }
           return future.get();

       } catch (InterruptedException | ExecutionException e) {
           throw new RuntimeException(e);
       } finally {
           lock.unlock();
       }

    }

    private void refreshCounter(){
        System.out.println("Refreshing counter");
        try{
            lock.lock();
            windowStart.set(System.currentTimeMillis());
            count.set(0);
            while(!waitingQueue.isEmpty()){
                CompletableFuture<Boolean> future = waitingQueue.poll();
                future.complete(true);
                count.incrementAndGet();
            }
            awaitCapacity.signalAll();

        }finally {
            lock.unlock();
        }

    }
}
