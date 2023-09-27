package polis.callbacks.inlinekeyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.Callback;
import polis.callbacks.CallbackHandler;
import polis.callbacks.inlinekeyboard.CallbackParser;
import polis.callbacks.inlinekeyboard.CallbackType;

/**
 * Данные колбеки отличаются тем, что обрабатываемая ими информация сериализуется в строку, типизирована и структурирована
 * Поэтому их интерфейс дополняется методом, принимающим строку,
 * а также дополняется парсером, который может это строку преобразовать в объект колбека
 *
 * @param <CB> - Callback
 */
public interface InlineKeyboardCallbackHandler<CB extends Callback> extends CallbackHandler<CB> {
    void handleCallback(Message message, String callbackData) throws TelegramApiException;

    CallbackParser<CB> callbackParser();

    CallbackType callbackType();
}
