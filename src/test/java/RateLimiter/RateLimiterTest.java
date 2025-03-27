package RateLimiter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RateLimiterTest {
    @Test
    public void testRequestsWithinLimit() {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(5, 1000, TimeUnit.MILLISECONDS);
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest());// "Request " + i + " should be allowed");
        }
    }

    @Test
    public void testExceedingLimit() {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(3, 1000, TimeUnit.MILLISECONDS);
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        assertFalse(rateLimiter.allowRequest());// "4th request should be rejected");
    }

    @Test
    public void testWindowReset() throws InterruptedException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(2, 1000, TimeUnit.MILLISECONDS);
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        Thread.sleep(1000);
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
    }

    @Test
    public void testMultiThreading() throws InterruptedException, ExecutionException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(10, 300, TimeUnit.MILLISECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            results.add(executor.submit(rateLimiter::allowRequest));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        for (Future<Boolean> result : results) {
            assertTrue(result.get());// "All requests should be allowed");
        }
    }

    @Test
    public void testMultiThreading_hugeNumber() throws InterruptedException, ExecutionException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(1000, 3000, TimeUnit.MILLISECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            results.add((Future<Boolean>) executor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                rateLimiter.allowRequest();
            }));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        for (Future<Boolean> result : results) {
            assertTrue(result.get());// "All requests should be allowed");
        }
    }

    @Test
    public void testMultiThreadingWithExceedingRequests() throws InterruptedException, ExecutionException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(5, 1000,TimeUnit.MILLISECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            results.add(executor.submit(rateLimiter::allowRequest));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        int allowed = 0, rejected = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) allowed++;
            else rejected++;
        }

        assertEquals(5, allowed, "Only 5 requests should be allowed");
        assertEquals(5, rejected, "5 requests should be rejected");
    }
}
