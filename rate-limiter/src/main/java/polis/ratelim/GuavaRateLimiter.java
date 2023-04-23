package polis.ratelim;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class GuavaRateLimiter implements polis.ratelim.RateLimiter {

    private final double permitsPerSecond;
    private final int maxSize;
    private final Duration expirationTime;

    private final Cache<Long, RateLimiter> rateLimiters;

    public GuavaRateLimiter(double permitsPerSecond, int cacheRecordsMaxSize, Duration recordExpirationTime){
        this.permitsPerSecond = permitsPerSecond;
        this.maxSize = cacheRecordsMaxSize;
        this.expirationTime = recordExpirationTime;

        rateLimiters = CacheBuilder.newBuilder()
                .maximumSize(cacheRecordsMaxSize)
                .expireAfterWrite(recordExpirationTime)
                .build();
    }

    @Override
    public boolean allowRequest(long userId) throws ExecutionException {
        RateLimiter rateLimiter = rateLimiters.get(userId, this::createLimiter);
        return rateLimiter.tryAcquire();
    }

    private RateLimiter createLimiter() {
        return RateLimiter.create(permitsPerSecond);
    }
}
