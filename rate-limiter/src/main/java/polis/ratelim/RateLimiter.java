package polis.ratelim;

import java.util.concurrent.ExecutionException;

public interface RateLimiter {
    boolean allowRequest(long userId) throws ExecutionException;
}
