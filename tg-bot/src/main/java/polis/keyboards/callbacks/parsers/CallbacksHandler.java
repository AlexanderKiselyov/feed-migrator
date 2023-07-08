package polis.keyboards.callbacks.parsers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.objects.Callback;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CallbacksHandler {
    private final Map<String, ACallbackParser<? extends Callback>> parsers;

    @Autowired
    public CallbacksHandler(List<ACallbackParser<?>> parsers) {
        this.parsers = parsers.stream().collect(Collectors.toMap(
                parser -> parser.callbackType.stringKey,
                Function.identity()
        ));
    }

    public Callback handleCallback(String callbackData) {
        ACallbackParser.ParsingContext ctx = ACallbackParser.parseCallbackString(callbackData);
        ACallbackParser<? extends Callback> parser = parsers.get(ctx.type);
        if (parser == null) {
            return null;
        }
        return parser.fromText(ctx);
    }

}
