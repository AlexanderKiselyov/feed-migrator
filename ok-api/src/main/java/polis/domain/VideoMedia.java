package polis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public final class VideoMedia extends Media implements Serializable {

    @JsonProperty("list")
    public Collection<Video> videos;

    public VideoMedia(Collection<Video> videos) {
        super("movie");
        this.videos = videos;
    }

    public VideoMedia(int videosCount) {
        this(new ArrayList<>(videosCount));
    }

    public void addVideo(Video video) {
        videos.add(video);
    }
}
