package polis.keyboards.callbacks.objects;

public final class AutopostingCallback extends Callback {
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
