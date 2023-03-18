package polis.ok.domain;

public final class LinkMedia extends Media{
    public final String url;

    public LinkMedia(String url) {
        super("link");
        this.url = url;
    }
}
