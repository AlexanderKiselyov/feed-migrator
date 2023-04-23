package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import polis.datacheck.DataCheck;
import polis.util.SocialMedia;
import polis.util.State;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class Notifications extends Command {
    private static final String NOTIFICATIONS_MSG = """
            Включите уведомления, чтобы получать информацию о публикации Ваших постов.
            """;
    private static final String NOTIFICATIONS_MSG_INLINE = """
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

    @Autowired
    private DataCheck dataCheck;

    private static final int rowsCount = 1;
    private final Logger logger = LoggerFactory.getLogger(Autoposting.class);

    public Notifications() {
        super(State.Notifications.getIdentifier(), State.Notifications.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    NOTIFICATIONS_MSG,
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            String notificationsEnable = "";
            if (currentGroup.getSocialMedia() == SocialMedia.OK) {
                notificationsEnable = String.format(NOTIFICATIONS_MSG_INLINE,
                        currentChannel.getChannelUsername(),
                        dataCheck.getOKGroupName(currentGroup.getGroupId(), currentAccount.getAccessToken()),
                        currentGroup.getGroupName());
            } else {
                logger.error(String.format("Social media incorrect: %s",
                        currentGroup.getSocialMedia()));
            }
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    notificationsEnable,
                    rowsCount,
                    commandsForKeyboard,
                    getNotificationsButtons(currentChannel.getChannelId()));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_CURRENT_TG_CHANNEL, State.MainMenu.getIdentifier()),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getNotificationsButtons(Long id) {
        return new String[]{
                "Да",
                String.format("notifications %s 0", id),
                "Нет",
                String.format("notifications %s 1", id)
        };
    }
}
