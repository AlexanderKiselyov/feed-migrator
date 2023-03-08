package polis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

public class PhotoMedia extends Media {

    @JsonProperty("list")
    public final Collection<? extends Photo> photoIDS;

    public PhotoMedia(Collection<? extends Photo> photos) {
        super(Type.PHOTO);
        this.photoIDS = photos;
    }

}
