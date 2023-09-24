package polis.keyboards.callbacks;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.callbacks.objects.Callback;

/**
 * Сущность, умеющая обработать и выполнить специфичную логику для объекта колбека.
 * @param <CB> Callback
 */
public interface CallbackHandler<CB extends Callback> {
    void handleCallback(Message message, CB callback) throws TelegramApiException;

    CallbackType callbackType();
}
