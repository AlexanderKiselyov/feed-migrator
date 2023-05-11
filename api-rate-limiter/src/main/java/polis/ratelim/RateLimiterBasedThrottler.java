package polis.ratelim;

public class RateLimiterBasedThrottler implements Throttler {
    private final RateLimiter rateLimiter;

    public RateLimiterBasedThrottler(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void throttle(long id, Runnable runnable) {
        if (rateLimiter.allowRequest(id)) {
            runnable.run();
        }
    }
}
