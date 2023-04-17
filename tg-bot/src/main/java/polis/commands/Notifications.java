package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.ok.OKDataCheck;
import polis.util.*;

import java.util.Map;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class Notifications extends Command {
    private static final String NOTIFICATIONS_MSG = """
            Включите уведомления, чтобы получать информацию о публикации Ваших постов.
            """;
    private static final String NOTIFICATIONS_MSG_INLINE = """
            Включить данную функцию для Телеграм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup;
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final OKDataCheck okDataCheck;
    private static final int rowsCount = 1;
    private final Logger logger = LoggerFactory.getLogger(Autoposting.class);

    public Notifications(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel,
                         Map<Long, SocialMediaGroup> currentSocialMediaGroup,
                         Map<Long, AuthData> currentSocialMediaAccount,
                         OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
        this.currentSocialMediaGroup = currentSocialMediaGroup;
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.get(chat.getId()) != null) {
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
            if (Objects.requireNonNull(currentSocialMediaGroup.get(chat.getId()).getSocialMedia()) == SocialMedia.OK) {
                notificationsEnable = String.format(NOTIFICATIONS_MSG_INLINE,
                        currentTgChannel.get(chat.getId()).getTelegramChannelUsername(),
                        okDataCheck.getOKGroupName(currentSocialMediaGroup.get(chat.getId()).getId(),
                                currentSocialMediaAccount.get(chat.getId()).getAccessToken()),
                        currentSocialMediaGroup.get(chat.getId()).getSocialMedia().getName());
            } else {
                logger.error(String.format("Social media incorrect: %s",
                        currentSocialMediaGroup.get(chat.getId()).getSocialMedia()));
            }
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    notificationsEnable,
                    rowsCount,
                    commandsForKeyboard,
                    getNotificationsButtons(currentTgChannel.get(chat.getId()).getTelegramChannelId()));
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
