package polis.callbacks.inlinekeyboard.objects;

import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.TypedCallback;

public final class GoBackCallback implements TypedCallback {
    public static final GoBackCallback INSTANCE = new GoBackCallback();

    @Override
    public CallbackType type() {
        return CallbackType.GO_BACK;
    }
}
