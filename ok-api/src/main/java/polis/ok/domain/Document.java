package polis.ok.domain;

import java.io.Serializable;

public class Document implements Serializable {
    public final long id;

    public Document(long id) {
        this.id = id;
    }
}
