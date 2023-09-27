package polis.callbacks.inlinekeyboard;

import polis.callbacks.Callback;

public interface CallbackParser<CB extends Callback> {

    String toText(CB callback);

    CB fromText(String callbackString);

    CallbackType callbackType();

}
