package polis.bot;

public interface TgNotificator {
    void sendNotification(long userChatId, String message);
}
