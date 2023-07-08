package polis.keyboards.callbacks.parsers;

import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.Callback;

import java.util.Arrays;
import java.util.List;

public abstract class ACallbackParser<CB extends Callback> implements CallbackParser<CB> {
    private static final int CALLBACK_TYPE_INDEX = 0;
    private static final int META_FIELDS_COUNT = 1;
    protected static final String FIELDS_SEPARATOR = " ";

    protected final int dataFieldsCount;
    protected final CallbackType callbackType;

    protected ACallbackParser(CallbackType callbackType, int dataFieldsCount) {
        this.callbackType = callbackType;
        this.dataFieldsCount = dataFieldsCount;
    }

    protected abstract String toText2(CB callback);

    protected abstract CB fromText2(List<String> data);

    @Override
    public String toText(CB callback) {
        return callbackType.stringKey + FIELDS_SEPARATOR + toText2(callback);
    }

    @Override
    public CB fromText(String callbackString) {
        ParsingContext ctx = parseCallbackString(callbackString);
        return fromText(ctx);
    }

    CB fromText(ParsingContext ctx) {
        List<String> data = Arrays.asList(ctx.data).subList(META_FIELDS_COUNT, dataFieldsCount);
        return fromText2(data);
    }

    static ParsingContext parseCallbackString(String callbackString) {
        String[] data = callbackString.split(FIELDS_SEPARATOR);
        String type = data[CALLBACK_TYPE_INDEX];
        return new ParsingContext(type, data);
    }

    static class ParsingContext {
        public final String type;
        private final String[] data;

        public ParsingContext(String type, String[] data) {
            this.type = type;
            this.data = data;
        }
    }
}
