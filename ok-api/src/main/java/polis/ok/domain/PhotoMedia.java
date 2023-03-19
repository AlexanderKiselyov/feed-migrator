package polis.ok.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

public final class PhotoMedia extends Media {

    @JsonProperty("list")
    public final Collection<Photo> photos;

    public PhotoMedia(Collection<Photo> photos) {
        super("photo");
        this.photos = photos;
    }

}
