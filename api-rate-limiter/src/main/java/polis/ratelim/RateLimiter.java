package polis.ratelim;

/**
 * Простой интерфейс, который поможет ограничить запросы к апи соцсетей.
 */
public interface RateLimiter {
    boolean allowRequest(long userId);
}
