package polis.keyboards.callbacks.objects;

public final class NotificationsCallback extends Callback {
    public final long chatId;
    public final boolean isEnabled;

    public NotificationsCallback(long chatId, boolean isEnabled) {
        this.chatId = chatId;
        this.isEnabled = isEnabled;
    }
}
