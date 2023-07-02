package polis.keyboards.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.objects.Callback;
import polis.keyboards.callbacks.parsers.CallbackParser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CallBacksHandler {
    private static final int CALLBACK_TYPE_INDEX = 0;
    private static final int META_FIELDS_COUNT = 1;

    private final Map<String, CallbackParser<? extends Callback>> parsers;

    @Autowired
    public CallBacksHandler(List<CallbackParser<?>> parsers) {
        this.parsers = parsers.stream().collect(Collectors.toMap(
                parser -> parser.callbackType().stringKey,
                parser -> parser
        ));
    }

    public <CB extends Callback> CB handleCallback(String callbackData) {
        String[] data = callbackData.split(CallbackParser.FIELDS_SEPARATOR);
        String type = data[CALLBACK_TYPE_INDEX];

        CallbackParser<? extends Callback> parser = parsers.get(type);
        List<String> dataList = Arrays.asList(data).subList(META_FIELDS_COUNT, parser.dataFieldsCount());
        Callback callback = parser.fromText(dataList);
        return (CB) callback;
    }
}
