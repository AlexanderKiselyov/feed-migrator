package polis.bot;

public interface TgNotificator {
    void sendMessage(long ownerChatId, long channelId, String message);
}
