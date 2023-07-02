package polis.keyboards.callbacks.objects;

public final class TgChannelCallback extends Callback {
    public final long channelId;
    public final boolean isClickedForDeletion;

    public TgChannelCallback(long channelId, boolean isClickedForDeletion) {
        this.channelId = channelId;
        this.isClickedForDeletion = isClickedForDeletion;
    }
}
