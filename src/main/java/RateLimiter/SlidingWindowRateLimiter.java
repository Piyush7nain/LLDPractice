package RateLimiter;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowRateLimiter implements RateLimiter {

    private final int limit;
    private final int windowSize;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger windowCount = new AtomicInteger(0);
    private final ConcurrentLinkedDeque<Long> window = new ConcurrentLinkedDeque<>();
    public SlidingWindowRateLimiter(int limit, int windowSize) {
        assert limit > 0;
        assert windowSize > 0;
        this.limit = limit;
        this.windowSize = windowSize;
    }

    @Override
    public boolean allowRequest() {
        try {
            lock.lock();
            long now = System.currentTimeMillis();
            while(!window.isEmpty() && (now - window.peekFirst() > windowSize)){
                window.pollFirst();
                windowCount.decrementAndGet();
            }
            if (windowCount.get() < limit) {
                window.addLast(System.currentTimeMillis());
                windowCount.incrementAndGet();
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }
}
