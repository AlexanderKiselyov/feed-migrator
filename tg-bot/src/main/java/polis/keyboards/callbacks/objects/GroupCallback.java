package polis.keyboards.callbacks.objects;

public final class GroupCallback extends Callback {
    public final long groupId;
    public final boolean isClickForDeletion;
    public final String socialMedia;

    public GroupCallback(long groupId, boolean isClickForDeletion, String socialMedia) {
        this.groupId = groupId;
        this.isClickForDeletion = isClickForDeletion;
        this.socialMedia = socialMedia;
    }
}
