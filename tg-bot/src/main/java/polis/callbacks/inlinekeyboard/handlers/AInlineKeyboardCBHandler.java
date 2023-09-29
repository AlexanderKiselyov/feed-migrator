package polis.callbacks.inlinekeyboard.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.inlinekeyboard.CallbackParser;
import polis.callbacks.inlinekeyboard.InlineKeyboardCallbackHandler;
import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.inlinekeyboard.TypedCallback;

@Component
public abstract class AInlineKeyboardCBHandler<CB extends TypedCallback>
        extends UtilCallbackHandler<CB>
        implements InlineKeyboardCallbackHandler<CB> {

    protected CallbackParser<CB> callbackParser;

    @Override
    public void handleCallback(Message message, String callbackData) throws TelegramApiException {
        CallbackParser<CB> parser = callbackParser;
        CB callback = parser.fromText(callbackData);
        handleCallback(message, callback);
    }
}
