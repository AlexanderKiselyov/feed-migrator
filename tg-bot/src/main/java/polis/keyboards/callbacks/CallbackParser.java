package polis.keyboards.callbacks;

import polis.keyboards.callbacks.objects.Callback;

public interface CallbackParser<CB extends Callback> {

    String toText(CB callback);

    CB fromText(String callbackString);

    CallbackType callbackType();

}
