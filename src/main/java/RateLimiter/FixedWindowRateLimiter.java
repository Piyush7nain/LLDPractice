package RateLimiter;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class FixedWindowRateLimiter implements RateLimiter {
    private final int windowSize;
    private final TimeUnit timeUnit;
    private AtomicInteger count;
    private final int limit;
    private ScheduledExecutorService  refreshService;
    public FixedWindowRateLimiter(int limit, int windowSize, TimeUnit timeUnit) {
        this.limit = limit;
        this.windowSize = windowSize;
        this.timeUnit = timeUnit;
        this.count = new AtomicInteger(0);
        this.refreshService = new ScheduledThreadPoolExecutor(1);
        refreshService.scheduleAtFixedRate(this::refreshCounter,this.windowSize,this.windowSize,this.timeUnit);
    }
    @Override
    public boolean allowRequest() {
        if(count.get() >= limit){
            System.out.println("Limits Exceeded blocking request");
            return false;
        }
        System.out.println("Serving request number " +count.incrementAndGet());
        return true;
    }

    private void refreshCounter(){
        System.out.println("Refreshing counter");
        this.count.set(0);
    }
}
