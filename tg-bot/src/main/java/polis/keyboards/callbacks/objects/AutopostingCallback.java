package polis.keyboards.callbacks.objects;

public final class AutopostingCallback extends Callback {
    public final long chatId;
    public final long channelId;
    public final boolean enableOrDisable;

    public AutopostingCallback(long chatId, long channelId, boolean enableOrDisable) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.enableOrDisable = enableOrDisable;
    }
}
