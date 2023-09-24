package polis.keyboards.callbacks.handlers.inlinekeyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.handlers.InlineKeyboardCallbackHandler;
import polis.keyboards.callbacks.handlers.UtilCallbackHandler;
import polis.keyboards.callbacks.objects.Callback;

@Component
public abstract class AReplyKeyboardCbHandler<CB extends Callback> extends UtilCallbackHandler<CB> implements InlineKeyboardCallbackHandler<CB> {
    protected CallbackParser<CB> callbackParser;

    @Override
    public CallbackParser<CB> callbackParser() {
        return callbackParser;
    }

    @Override
    public void handleCallback(Message message, String callbackData) throws TelegramApiException {
        CallbackParser<CB> parser = callbackParser();
        CB callback = parser.fromText(callbackData);
        handleCallback(message, callback);
    }
}
