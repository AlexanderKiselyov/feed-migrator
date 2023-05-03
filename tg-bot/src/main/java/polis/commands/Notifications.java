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
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

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

    private static final String WRONG_SOCIAL_MEDIA_MSG = """
            Социальная сеть неверная.""";
    private final CommandUtils commandUtils;

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    private static final int rowsCount = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Notifications.class);

    public Notifications() {
        super(State.Notifications.getIdentifier(), State.Notifications.getDescription());
        commandUtils = new CommandUtils();
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
            String notificationsEnable;
            String groupName = commandUtils.getGroupName(currentAccount, currentGroup);
            switch (currentGroup.getSocialMedia()) {
                case OK, VK -> notificationsEnable = String.format(
                        NOTIFICATIONS_MSG_INLINE,
                        currentChannel.getChannelUsername(),
                        groupName,
                        currentGroup.getGroupName()
                );
                default -> {
                    LOGGER.error(String.format("Social media incorrect: %s", currentGroup.getSocialMedia()));
                    notificationsEnable = WRONG_SOCIAL_MEDIA_MSG;
                }
            }
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    notificationsEnable,
                    rowsCount,
                    commandsForKeyboard,
                    getButtonsForNotificationsOptions(currentChannel.getChannelId()));
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

    private String[] getButtonsForNotificationsOptions(Long id) {
        return new String[]{
                "Да",
                String.format("notifications %s 0", id),
                "Нет",
                String.format("notifications %s 1", id)
        };
    }
}
