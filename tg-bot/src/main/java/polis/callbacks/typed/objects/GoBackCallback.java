package polis.callbacks.typed.objects;

import polis.callbacks.typed.CallbackType;
import polis.callbacks.typed.TypedCallback;

public final class GoBackCallback implements TypedCallback {
    public static final GoBackCallback INSTANCE = new GoBackCallback();

    @Override
    public CallbackType type() {
        return CallbackType.GO_BACK;
    }
}
