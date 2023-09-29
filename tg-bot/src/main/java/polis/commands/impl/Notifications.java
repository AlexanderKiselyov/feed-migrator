package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.callbacks.typed.objects.NotificationsCallback;
import polis.callbacks.typed.parsers.NotificationCallbackParser;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class Notifications extends Command {
    private static final String NOTIFICATIONS_MSG = """
            Включите уведомления, чтобы получать информацию о публикации Ваших постов.
            Включить данную функцию для Телеграмм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            По какой-то причине не хватает информации о выбранной группе или телеграмм каналу
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private NotificationCallbackParser notificationCallbackParser;

    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    @Override
    public IState state() {
        return State.Notifications;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        ChannelGroup currentGroup = context.currentGroup();
        CurrentChannel currentChannel = context.currentChannel();
        if (currentChannel == null || currentGroup == null) {
            sendAnswerWithReplyKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    String.format(NO_CURRENT_TG_CHANNEL, State.MainMenu.getIdentifier()),
                    ROWS_COUNT,
                    KEYBOARD_COMMANDS_IN_ERROR_CASE,
                    loggingInfo(user.getUserName())
            );
            return;
        }
        String groupName = currentGroup.getGroupName();
        String notificationsEnable = String.format(NOTIFICATIONS_MSG, currentChannel.getChannelUsername(),
                groupName, currentGroup.getGroupName());
        sendAnswerWithInlineKeyboard(
                absSender,
                chat.getId(),
                notificationsEnable,
                ROWS_COUNT,
                getButtonsForNotificationsOptions(currentChannel.getChannelId()),
                loggingInfo(user.getUserName())
        );
    }

    private List<String> getButtonsForNotificationsOptions(Long id) {
        return List.of(
                YES_ANSWER,
                notificationCallbackParser.toText(new NotificationsCallback(id, true)),
                NO_ANSWER,
                notificationCallbackParser.toText(new NotificationsCallback(id, false))
        );
    }
}
