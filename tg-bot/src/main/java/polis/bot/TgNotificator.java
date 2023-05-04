package polis.bot;

public interface TgNotificator {
    void sendNotification(long userChatId, long channelId, String message);
}
