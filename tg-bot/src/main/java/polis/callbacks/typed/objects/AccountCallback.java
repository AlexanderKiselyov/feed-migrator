package polis.callbacks.typed.objects;

import polis.callbacks.typed.CallbackType;
import polis.callbacks.typed.TypedCallback;

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
