package polis.data_check.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Attachment implements Serializable {
    public final Collection<Media> media;

    public Attachment(Collection<Media> media) {
        this.media = media;
    }

    public Attachment() {
        this.media = new ArrayList<>();
    }

    public void addMedia(Media media) {
        this.media.add(media);
    }
}
