package polis.ok.domain;

import java.util.Collection;

public class Attachment {
    public final Collection<Media> media;

    public Attachment(Collection<Media> media) {
        this.media = media;
    }
}
