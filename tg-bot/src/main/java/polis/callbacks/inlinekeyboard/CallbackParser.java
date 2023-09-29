package polis.callbacks.inlinekeyboard;

public interface CallbackParser<CB extends TypedCallback> {

    String toText(CB callback);

    CB fromText(String callbackString);
}
