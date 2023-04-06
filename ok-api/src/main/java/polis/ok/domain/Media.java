package polis.ok.domain;

import java.io.Serializable;

public abstract sealed class Media implements Serializable permits AnimationMedia, LinkMedia, PhotoMedia, PollMedia, TextMedia, VideoMedia {
    public final String type;

    public Media(String type) {
        this.type = type;
    }
}
