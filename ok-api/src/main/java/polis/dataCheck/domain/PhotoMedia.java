package polis.dataCheck.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public final class PhotoMedia extends Media implements Serializable {

    @JsonProperty("list")
    public final Collection<Photo> photos;

    public PhotoMedia(Collection<Photo> photos) {
        super("photo");
        this.photos = photos;
    }

    public PhotoMedia(int photosCount) {
        this(new ArrayList<>(photosCount));
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

}
