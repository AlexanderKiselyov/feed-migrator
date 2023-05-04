package polis.ratelim;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("BetaApi")
public class GuavaRateLimiter implements polis.ratelim.RateLimiter {

    private final double permitsPerSecond;

    private final Cache<Long, RateLimiter> rateLimiters;

    public GuavaRateLimiter(double permitsPerSecond, int recordsMaxSize, Duration recordExpirationTime){
        this.permitsPerSecond = permitsPerSecond;

        rateLimiters = CacheBuilder.newBuilder()
                .maximumSize(recordsMaxSize)
                .expireAfterWrite(recordExpirationTime)
                .build();
    }

    @Override
    public boolean allowRequest(long userId) {
        RateLimiter rateLimiter;
        try {
            rateLimiter = rateLimiters.get(userId, this::createLimiter);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
        return rateLimiter.tryAcquire();
    }

    private RateLimiter createLimiter() {
        return RateLimiter.create(permitsPerSecond);
    }
}
