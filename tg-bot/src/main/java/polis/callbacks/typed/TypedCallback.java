package polis.callbacks.typed;

import polis.callbacks.Callback;

import java.io.Serializable;

/**
 * Данные колбеки отличаются своей типизированностью и структурированостью
 * Умеют возвращать свой тип (CallbackType)
 * Сейчас основное их применение это обработка нажатий кнопок Inline клавиатуры,
 * для этого калбеки сериализуются в строку при помощи CallbackParser
 *
 * @see CallbackParser
 * @see TypedCallbackHandler
 * @see org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton#getCallbackData()
 */
public interface TypedCallback extends Callback, Serializable {
    CallbackType type();
}
