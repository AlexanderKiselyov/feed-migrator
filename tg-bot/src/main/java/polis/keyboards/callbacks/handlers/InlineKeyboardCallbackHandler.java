package polis.keyboards.callbacks.handlers;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.callbacks.CallbackHandler;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.objects.Callback;

/**
 * Данные колбеки отличаются тем, чтобы обрабатываемая ими информация сериализуется в строку, типизирована и структурирована
 * Поэтому их интерфейс добавляется методом, принимающим строку,
 * а также дополняется парсером, который может это струку преобразовать в инстанс колбека
 *
 * @param <CB> - Callback
 */
public interface InlineKeyboardCallbackHandler<CB extends Callback> extends CallbackHandler<CB> {
    void handleCallback(Message message, String callbackData) throws TelegramApiException;

    CallbackParser<CB> callbackParser();
}
