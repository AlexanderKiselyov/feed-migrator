package polis.keyboards.callbacks.objects;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
 * @see polis.keyboards.callbacks.CallbackType,
 * @see polis.keyboards.callbacks.handlers.InlineKeyboardCallbackHandler
 * @see polis.keyboards.callbacks.handlers.ReplyKeyboardCallbackHandler
 * @see polis.util.IState
 */
public abstract sealed class Callback implements Serializable
        permits AccountCallback, AutopostingCallback, GoBackCallback, GroupCallback, NotificationsCallback, ReplyKeyboardCallback, TgChannelCallback, YesNoCallback {
}
