package polis.ratelim;

/**
 * Простой интерфейс, который поможет не спамить юзеру сообщениями в ответ на его каждое спам-сообщение
 * @see RateLimiter#allowRequest(long)
 */
public interface Throttler {
    void throttle(long id, Runnable runnable);
}
