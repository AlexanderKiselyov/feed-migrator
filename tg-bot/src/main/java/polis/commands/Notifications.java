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
import polis.util.State;

import java.util.List;

@Component
public class Notifications extends Command {
    private static final String NOTIFICATIONS_MSG = """
            Включите уведомления, чтобы получать информацию о публикации Ваших постов.""";
    private static final String NOTIFICATIONS_INLINE_MSG = """
            Включить данную функцию для Телеграм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboardInErrorCase = List.of(State.MainMenu.getDescription());

    public Notifications() {
        super(State.Notifications.getIdentifier(), State.Notifications.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = currentGroup.getGroupName();
            String notificationsEnable = String.format(NOTIFICATIONS_INLINE_MSG, currentChannel.getChannelUsername(),
                    groupName, currentGroup.getGroupName());
            sendAnswerWithInlineKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    NOTIFICATIONS_MSG,
                    notificationsEnable,
                    ROWS_COUNT,
                    getButtonsForNotificationsOptions(currentChannel.getChannelId()));
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                String.format(NO_CURRENT_TG_CHANNEL, State.MainMenu.getIdentifier()),
                ROWS_COUNT,
                commandsForKeyboardInErrorCase);
    }

    private List<String> getButtonsForNotificationsOptions(Long id) {
        return List.of(
                "Да",
                String.format("notifications %s 0", id),
                "Нет",
                String.format("notifications %s 1", id)
        );
    }
}
