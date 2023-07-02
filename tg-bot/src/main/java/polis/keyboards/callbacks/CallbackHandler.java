package polis.keyboards.callbacks;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.callbacks.objects.Callback;

public interface CallbackHandler<CB extends Callback> {
    void handleCallback(Message message, String callbackData) throws TelegramApiException;

    CallbackType callbackType();
}
