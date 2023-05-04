package polis.ratelim;

import java.util.concurrent.ExecutionException;

/**
 * Простой интерфейс, который поможет ограничить запросы к апи соцсетей
 */
public interface RateLimiter {
    boolean allowRequest(long userId) throws ExecutionException;
}
