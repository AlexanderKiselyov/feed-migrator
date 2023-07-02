package polis.keyboards.callbacks.objects;

import java.io.Serializable;

public abstract sealed class Callback implements Serializable permits AccountCallback {
    protected static final String CALLBACK_DATA_SEPARATOR = " ";
}
