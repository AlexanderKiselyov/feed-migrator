package polis.ok.domain;

public final class TextMedia extends Media{
    public final String text;

    public TextMedia(String text) {
        super("text");
        this.text = text;
    }
}
