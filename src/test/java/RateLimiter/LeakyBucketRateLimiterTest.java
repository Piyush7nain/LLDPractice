package RateLimiter;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class LeakyBucketRateLimiterTest {

    @Test
    public void testRequestsWithinLimit() throws InterruptedException {
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(5, 200);

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest(), "Request " + i + " should be allowed");
            Thread.sleep(200); // Ensure request leakage happens
        }
    }

    @Test
    public void testExceedingQueueCapacity() throws InterruptedException {
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(2, 300);

        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());

        // These should be queued
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());

        // Queue is full, this request should be dropped
        assertFalse(rateLimiter.allowRequest(), "5th request should be dropped");
    }

    @Test
    public void testMultiThreadedRequests() throws InterruptedException, ExecutionException {
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(5, 100);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            results.add(executor.submit(rateLimiter::allowRequest));
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        int allowed = 0, rejected = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) allowed++;
            else rejected++;
        }

        assertEquals(5, allowed, "Only 5 requests should be allowed");
        assertEquals(5, rejected, "5 requests should be dropped");
    }

    @Test
    public void testBackgroundProcessing() throws InterruptedException {
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(3, 500);

        // Add requests quickly
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());

        // Wait for leaking
        Thread.sleep(1500);

        // Now we should be able to process new requests
        assertTrue(rateLimiter.allowRequest());
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new LeakyBucketRateLimiter(3, 1000);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            results.add(executor.submit(rateLimiter::allowRequest));
            Thread.sleep(500);
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        Thread.sleep(30000);
        rateLimiter.allowRequest();

    }
}
