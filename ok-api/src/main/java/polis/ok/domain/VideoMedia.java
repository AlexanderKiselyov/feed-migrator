package polis.ok.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

public final class VideoMedia extends Media{

    @JsonProperty("list")
    public Collection<Video> videos;

    public VideoMedia(Collection<Video> videos) {
        super("movie");
        this.videos = videos;
    }
}
