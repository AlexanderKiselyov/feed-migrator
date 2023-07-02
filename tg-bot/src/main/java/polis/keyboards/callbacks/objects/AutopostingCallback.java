package polis.keyboards.callbacks.objects;

public final class AutopostingCallback extends Callback {
    public final long chatId;
    public final long channelId;
    public final boolean isEnabled;

    public AutopostingCallback(long chatId, long channelId, boolean isEnabled) {
        this.chatId = chatId;
        this.channelId = channelId;
        this.isEnabled = isEnabled;
    }
}
