package polis.keyboards.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.callbacks.objects.Callback;
import polis.keyboards.callbacks.parsers.ACallbackParser;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CallbacksHandlerHelper {
    private final Map<String, CallbackHandler<? extends Callback>> handlersByType;

    @Autowired
    public CallbacksHandlerHelper(List<CallbackHandler<?>> handlers) {
        this.handlersByType = handlers.stream().collect(Collectors.toMap(
                handler -> handler.callbackType().stringKey,
                Function.identity()
        ));
    }

    public void handleCallback(Message message, String callbackString) throws TelegramApiException {
        ACallbackParser.CallbackTypeAndData typeAndData = ACallbackParser.parseTypeAndData(callbackString);
        CallbackHandler<? extends Callback> handler = handlersByType.get(typeAndData.type());
        if (handler == null) {
            throw new IllegalArgumentException("Cannot find handler for callback with type %s for callback %s"
                    .formatted(typeAndData.type(), callbackString)
            );
        }
        handler.handleCallback(message, typeAndData.data());
    }
}
