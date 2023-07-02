package polis.keyboards.callbacks.objects;

public final class AccountCallback extends Callback {
    public final long accountId;
    public final boolean isClickedForDeletion;
    public final String socialMedia;

    public AccountCallback(long accountId, boolean isClickedForDeletion, String socialMedia) {
        this.accountId = accountId;
        this.isClickedForDeletion = isClickedForDeletion;
        this.socialMedia = socialMedia;
    }
}
