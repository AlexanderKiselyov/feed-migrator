package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.Callback;

public final class TgChannelCallback implements Callback {
    public final long channelId;
    public final boolean isClickedForDeletion;

    public TgChannelCallback(long channelId, boolean isClickedForDeletion) {
        this.channelId = channelId;
        this.isClickedForDeletion = isClickedForDeletion;
    }
}
