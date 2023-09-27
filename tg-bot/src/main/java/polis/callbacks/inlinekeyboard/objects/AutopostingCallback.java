package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.Callback;

public final class AutopostingCallback implements Callback {
    public final long chatId;
    public final long channelId;
    public final boolean enable;

    public AutopostingCallback(long chatId, long channelId, boolean enable) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.enable = enable;
    }

    public boolean enable() {
        return enable;
    }

    public boolean disable() {
        return !enable;
    }

}
