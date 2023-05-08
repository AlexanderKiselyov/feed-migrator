package polis.ratelim;

/**
 * Простой интерфейс, который поможет ограничить запросы пользователя к апи соцсетей.
 */
public interface RateLimiter {
    boolean allowRequest(long id);
}
