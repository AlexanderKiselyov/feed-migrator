package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.Callback;

public final class AccountCallback implements Callback {
    public final long accountId;
    public final boolean isClickedForDeletion;
    public final String socialMedia;

    public AccountCallback(long accountId, boolean isClickedForDeletion, String socialMedia) {
        this.accountId = accountId;
        this.isClickedForDeletion = isClickedForDeletion;
        this.socialMedia = socialMedia;
    }
}
