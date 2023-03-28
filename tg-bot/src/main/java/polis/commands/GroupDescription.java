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

import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION = """
            Выбрана группа <b>%s</b> из соцсети %s.""";
    private static final String NO_VALID_GROUP = """
            Ошибка выбора группы.
            Пожалуйста, вернитесь в описание телеграмм канала (/%s) и выберите нужную группу.""";
    private final Map<Long, SocialMediaGroup> currentGroup;
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final OKDataCheck okDataCheck;
    private final Logger logger = LoggerFactory.getLogger(GroupDescription.class);

    public GroupDescription(String commandIdentifier, String description, Map<Long, SocialMediaGroup> currentGroup,
                            Map<Long, AuthData> currentSocialMediaAccount, OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentGroup = currentGroup;
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.okDataCheck = okDataCheck;
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
        if (currentGroup.get(chat.getId()) != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(GROUP_DESCRIPTION, groupName,
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
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
