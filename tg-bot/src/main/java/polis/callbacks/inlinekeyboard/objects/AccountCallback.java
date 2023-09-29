package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.TypedCallback;

public final class AccountCallback implements TypedCallback {
    public final long accountId;
    public final boolean isClickedForDeletion;
    public final String socialMedia;

    public AccountCallback(long accountId, boolean isClickedForDeletion, String socialMedia) {
        this.accountId = accountId;
        this.isClickedForDeletion = isClickedForDeletion;
        this.socialMedia = socialMedia;
    }

    @Override
    public CallbackType type() {
        return CallbackType.ACCOUNT_CHOSEN;
    }
}
