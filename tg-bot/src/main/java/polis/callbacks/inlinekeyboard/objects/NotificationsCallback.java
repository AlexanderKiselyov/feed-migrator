package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.Callback;

public final class NotificationsCallback implements Callback {
    public final long chatId;
    public final boolean isEnabled;

    public NotificationsCallback(long chatId, boolean isEnabled) {
        this.chatId = chatId;
        this.isEnabled = isEnabled;
    }
}
