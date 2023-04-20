package polis.data_check.domain;

import java.io.Serializable;

public final class LinkMedia extends Media implements Serializable {
    public final String url;

    public LinkMedia(String url) {
        super("link");
        this.url = url;
    }
}
