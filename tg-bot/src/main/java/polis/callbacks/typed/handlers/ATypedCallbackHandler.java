package polis.callbacks.typed.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.typed.CallbackParser;
import polis.callbacks.typed.TypedCallbackHandler;
import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.typed.TypedCallback;

@Component
public abstract class ATypedCallbackHandler<CB extends TypedCallback>
        extends UtilCallbackHandler<CB>
        implements TypedCallbackHandler<CB> {

    protected CallbackParser<CB> callbackParser;

    @Override
    public CallbackParser<CB> callbackParser() {
        return callbackParser;
    }
}
