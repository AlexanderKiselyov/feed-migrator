package polis.callbacks.typed;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.CallbackHandler;

/**
 * Хэндлер типизированного колбека
 * Знает тип (CallbackType) обрабатываемоего колбека
 * Всегда в наличии парсер, который может десерилизовать колбек из строки
 * Соответственно умеет обрабатывать ещё не десериализованный колбек
 *
 * @param <CB> - Callback
 */
public interface TypedCallbackHandler<CB extends TypedCallback> extends CallbackHandler<CB> {

    @Override
    void handleCallback(Message message, CB callback) throws TelegramApiException;

    CallbackType callbackType();

    CallbackParser<CB> callbackParser();

    /**
     * @param message
     * @param callbackData - string contating payload of callback (all data expect one related to CallbackType)
     * @throws TelegramApiException
     */
    default void handleCallback(Message message, String callbackData) throws TelegramApiException {
        CallbackParser<CB> parser = callbackParser();
        CB callback = parser.fromText(callbackData);
        handleCallback(message, callback);
    }
}
