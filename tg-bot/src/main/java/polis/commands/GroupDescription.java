package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.ok.OKDataCheck;
import polis.util.AuthData;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION = """
            Выбрана группа <b>%s</b> из социальной сети %s.
            Вы можете выбрать команду /autoposting для настройки автопостинга.""";
    private static final String GROUP_DESCRIPTION_EXTENDED = """
            Выбрана группа <b>%s</b> из социальной сети %s.
            Вы можете выбрать команду /autoposting для настройки автопостинга.
            Настроить уведомления об автоматически опубликованных постах можно с помощью команды  /notifications.""";
    private static final String NO_VALID_GROUP = """
            Ошибка выбора группы.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и выберите нужную группу.""";
    private final Map<Long, SocialMediaGroup> currentGroup;
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final Map<Long, Boolean> isAutoposting;
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final OKDataCheck okDataCheck;
    private final Logger logger = LoggerFactory.getLogger(GroupDescription.class);
    private int rowsCount = 1;
    private final List<String> commandsForKeyboard = new ArrayList<>();

    public GroupDescription(String commandIdentifier, String description, Map<Long, SocialMediaGroup> currentGroup,
                            Map<Long, AuthData> currentSocialMediaAccount, OKDataCheck okDataCheck,
                            Map<Long, Boolean> isAutoposting, Map<Long, TelegramChannel> currentTgChannel) {
        super(commandIdentifier, description);
        this.currentGroup = currentGroup;
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.okDataCheck = okDataCheck;
        this.isAutoposting = isAutoposting;
        this.currentTgChannel = currentTgChannel;
        this.commandsForKeyboard.add(State.Autoposting.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String groupName = "";
        switch (currentGroup.get(chat.getId()).getSocialMedia()) {
            case OK -> groupName = okDataCheck.getOKGroupName(currentGroup.get(chat.getId()).getId(),
                    currentSocialMediaAccount.get(chat.getId()).getAccessToken());
            default -> logger.error(String.format("Social media not found: %s",
                    currentGroup.get(chat.getId()).getSocialMedia()));
        }
        long channelId = currentTgChannel.get(chat.getId()).getTelegramChannelId();
        boolean isAutopostingEnable = isAutoposting.containsKey(channelId) && isAutoposting.get(channelId);
        String msgToSend = isAutopostingEnable ? GROUP_DESCRIPTION_EXTENDED : GROUP_DESCRIPTION;
        if (currentGroup.get(chat.getId()) != null) {
            if (isAutopostingEnable && !commandsForKeyboard.contains(State.Notifications.getDescription())) {
                commandsForKeyboard.add(State.Notifications.getDescription());
                rowsCount++;
            } else if (isAutoposting.containsKey(channelId) && !isAutoposting.get(channelId)
                    && commandsForKeyboard.contains(State.Notifications.getDescription())) {
                commandsForKeyboard.remove(State.Notifications.getDescription());
                rowsCount--;
            }
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(msgToSend, groupName,
                            currentGroup.get(chat.getId()).getSocialMedia().getName()),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_VALID_GROUP, State.TgChannelDescription.getIdentifier()),
                    super.rowsCount,
                    super.commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
