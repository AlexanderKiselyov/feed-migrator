package polis.domain;

import java.io.Serializable;

public final class Photo implements Serializable {
    public final String id;

    public Photo(String id) {
        this.id = id;
    }
}
