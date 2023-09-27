package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.Callback;

public final class GroupCallback implements Callback {
    public final long groupId;
    public final boolean isClickForDeletion;
    public final String socialMedia;

    public GroupCallback(long groupId, boolean isClickForDeletion, String socialMedia) {
        this.groupId = groupId;
        this.isClickForDeletion = isClickForDeletion;
        this.socialMedia = socialMedia;
    }
}
