package polis.keyboards.callbacks.objects;

import java.io.Serializable;

/**
 *
 * @see org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton#callbackData
 */
public abstract sealed class Callback implements Serializable
        permits AccountCallback, AutopostingCallback, GoBackCallback, GroupCallback, NotificationsCallback, ReplyKeyboardCallback, TgChannelCallback, YesNoCallback {
}
