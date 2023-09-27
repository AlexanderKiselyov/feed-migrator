package polis.callbacks;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import polis.callbacks.inlinekeyboard.InlineKeyboardCallbackHandler;
import polis.callbacks.messages.MessageCallbackHandler;
import polis.callbacks.inlinekeyboard.CallbackType;

import java.io.Serializable;

/**
 * В общем смысле Callback - это любая текстовая информация, не являющаяся командой,
 * что может прийти на сервер в результате взаимодействия пользователя с ботом
 * Например, это может быть:
 * <ul>
 *   <li>Текстовая информация, которую прислал пользователь по просьбе бота (код авторизации, ссылка на группу и т.п.)</li>
 *   <li>Данные, пришедшие после нажатия на кнопку Inline клавиатуры</li>
 * </ul>
 *
 * @see InlineKeyboardButton#getCallbackData()
 * @see CallbackType ,
 * @see InlineKeyboardCallbackHandler
 * @see MessageCallbackHandler
 * @see polis.util.IState
 */
public interface Callback extends Serializable {
}
