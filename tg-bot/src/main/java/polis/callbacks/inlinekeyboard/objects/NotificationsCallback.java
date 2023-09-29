package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.TypedCallback;

public final class NotificationsCallback implements TypedCallback {
    public final long chatId;
    public final boolean isEnabled;

    public NotificationsCallback(long chatId, boolean isEnabled) {
        this.chatId = chatId;
        this.isEnabled = isEnabled;
    }

    @Override
    public CallbackType type() {
        return CallbackType.NOTIFICATIONS;
    }
}
