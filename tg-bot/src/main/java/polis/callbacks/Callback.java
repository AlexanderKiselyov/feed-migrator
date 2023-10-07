package polis.callbacks;

import java.io.Serializable;

/**
 * В общем смысле Callback - это любая текстовая информация, не являющаяся командой,
 * что может прийти на сервер в результате взаимодействия пользователя с ботом
 * Например, это может быть:
 * <ul>
 *   <li>Текстовая информация, которую прислал пользователь по просьбе бота (код авторизации, ссылка на группу и т.п.)</li>
 *   <li>Данные, пришедшие после нажатия на кнопку Inline клавиатуры</li>
 * </ul>
 * See its inheritors:
 *
 * @see polis.callbacks.typed.TypedCallback
 * @see polis.callbacks.justmessages.SomeMessage
 */
public interface Callback extends Serializable {
}
