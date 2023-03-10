package polis.domain;

public class LinkMedia extends Media{
    public final String url;

    public LinkMedia(String url) {
        super(Type.LINK);
        this.url = url;
    }
}
