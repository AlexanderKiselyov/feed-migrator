package polis.domain;

import java.io.Serializable;

public final class TextMedia extends Media implements Serializable {
    public final String text;

    public TextMedia(String text) {
        super("text");
        this.text = text;
    }
}
