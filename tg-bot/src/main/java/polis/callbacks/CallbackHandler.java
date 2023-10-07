package polis.callbacks;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Сущность, умеющая обработать, выполнить специфичную логику соответсвующую объекту колбека
 *
 * @param <CB> Callback
 */
public interface CallbackHandler<CB extends Callback> {
    void handleCallback(Message message, CB callback) throws TelegramApiException;
}
