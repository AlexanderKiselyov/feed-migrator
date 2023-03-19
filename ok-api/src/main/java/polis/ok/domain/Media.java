package polis.ok.domain;

public abstract sealed class Media permits LinkMedia, PhotoMedia, PollMedia, TextMedia, VideoMedia {
    public final String type;

    public Media(String type) {
        this.type = type;
    }
}
