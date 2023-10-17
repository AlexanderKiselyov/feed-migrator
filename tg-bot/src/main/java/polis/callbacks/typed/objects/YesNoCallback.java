package polis.callbacks.typed.objects;

import polis.callbacks.typed.CallbackType;
import polis.callbacks.typed.TypedCallback;

public final class YesNoCallback implements TypedCallback {
    public static final YesNoCallback YES_CALLBACK = new YesNoCallback(true);
    public static final YesNoCallback NO_CALLBACK = new YesNoCallback(false);

    public final boolean yes;

    public YesNoCallback(boolean yes) {
        this.yes = yes;
    }

    public boolean yes() {
        return yes;
    }

    public boolean no() {
        return !yes;
    }

    @Override
    public CallbackType type() {
        return CallbackType.YES_NO_ANSWER;
    }
}
