package polis.callbacks.typed;

public interface CallbackParser<CB extends TypedCallback> {

    String toText(CB callback);

    CB fromText(String callbackString);
}
