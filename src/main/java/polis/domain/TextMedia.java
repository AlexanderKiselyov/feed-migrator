package polis.domain;

public class TextMedia extends Media{
    public final String text;

    public TextMedia(String text) {
        super(Type.TEXT);
        this.text = text;
    }
}
