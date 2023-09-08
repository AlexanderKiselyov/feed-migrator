package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.keyboards.callbacks.objects.NotificationsCallback;
import polis.keyboards.callbacks.parsers.NotificationCallbackParser;
import polis.util.State;

import java.util.List;

@Component
public class Notifications extends Command {
    private static final String NOTIFICATIONS_MSG = """
            Включите уведомления, чтобы получать информацию о публикации Ваших постов.
            Включить данную функцию для Телеграмм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            Телеграмм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final String ENABLE_NOTIFICATIONS = "notifications %s 0";
    private static final String DISABLE_NOTIFICATIONS = "notifications %s 1";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private NotificationCallbackParser notificationCallbackParser;

    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    public Notifications(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.Notifications.getIdentifier(), State.Notifications.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = currentGroup.getGroupName();
            String notificationsEnable = String.format(NOTIFICATIONS_MSG, currentChannel.getChannelUsername(),
                    groupName, currentGroup.getGroupName());
            sendAnswerWithInlineKeyboard(
                    absSender,
                    chat.getId(),
                    notificationsEnable,
                    ROWS_COUNT,
                    getButtonsForNotificationsOptions(currentChannel.getChannelId()),
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                String.format(NO_CURRENT_TG_CHANNEL, State.MainMenu.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
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
