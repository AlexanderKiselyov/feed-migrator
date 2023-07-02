package polis.keyboards.callbacks.parsers;

import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.Callback;

import java.util.List;

public interface CallbackParser<CB extends Callback> {
    String FIELDS_SEPARATOR = " ";

    String toText(CB callback);

    CB fromText(List<String> data);

    int dataFieldsCount();

    CallbackType callbackType();
}
