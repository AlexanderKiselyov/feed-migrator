package polis.ok.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;

public final class VideoMedia extends Media implements Serializable {

    @JsonProperty("list")
    public Collection<Video> videos;

    public VideoMedia(Collection<Video> videos) {
        super("movie");
        this.videos = videos;
    }

    public void addVideo(Video video) {
        videos.add(video);
    }
}
