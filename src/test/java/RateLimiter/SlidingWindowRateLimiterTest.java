package RateLimiter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlidingWindowRateLimiterTest {

    @Test
    public void testRequestsWithinLimit() {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(5, 1000);
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest(), "Request " + i + " should be allowed");
        }
    }

    @Test
    public void testExceedingLimit() {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(3, 1000);
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        assertFalse(rateLimiter.allowRequest(), "4th request should be rejected");
    }

    @Test
    public void testSlidingWindowEffect() throws InterruptedException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(3, 1000);
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());

        Thread.sleep(500);
        assertFalse(rateLimiter.allowRequest(), "4th request should be rejected");

        Thread.sleep(600); // Oldest request should expire
        assertTrue(rateLimiter.allowRequest(), "New request should be allowed");
    }

    @Test
    public void testMultiThreading() throws InterruptedException, ExecutionException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(10, 1000);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            results.add(executor.submit(rateLimiter::allowRequest));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        for (Future<Boolean> result : results) {
            assertTrue(result.get(), "All requests should be allowed");
        }
    }
}
