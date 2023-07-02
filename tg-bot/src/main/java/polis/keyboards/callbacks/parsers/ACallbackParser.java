package polis.keyboards.callbacks.parsers;

import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.Callback;

import java.util.Arrays;
import java.util.List;

public abstract class ACallbackParser<CB extends Callback> implements CallbackParser<CB> {
    private static final String TYPE_TO_DATA_SEPARATOR = " ";
    protected static final String FIELDS_SEPARATOR = " ";

    protected final int dataFieldsCount;

    protected ACallbackParser(int dataFieldsCount) {
        this.dataFieldsCount = dataFieldsCount;
    }

    protected abstract String toData(CB callback);

    protected abstract CB fromText(List<String> data);

    @Override
    public String toText(CB callback) {
        String callbackData = toData(callback);
        return appendType(callbackData, callbackType());
    }

    @Override
    public CB fromText(String callbackData) {
        List<String> data = Arrays.asList(callbackData.split(FIELDS_SEPARATOR));
        return fromText(data);
    }

    public static String appendType(String callBackData, CallbackType callbackType) {
        return callbackType.stringKey + TYPE_TO_DATA_SEPARATOR + callBackData;
    }

    public static CallbackTypeAndData parseTypeAndData(String callbackString) {
        String[] typeAndData = callbackString.split(TYPE_TO_DATA_SEPARATOR, 2);
        return new CallbackTypeAndData(typeAndData[0], typeAndData[1]);
    }

    public record CallbackTypeAndData(String type, String data) {
    }

}
