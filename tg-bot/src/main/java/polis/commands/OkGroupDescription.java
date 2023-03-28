package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.ok.OKDataCheck;
import polis.util.AuthData;
import polis.util.SocialMediaGroup;
import polis.util.State;

import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class OkGroupDescription extends Command {
    private static final String OK_GROUP_DESCRIPTION = """
            Выбрана ОК группа <b>%s</b>.""";
    private static final String NOT_VALID_SOCIAL_MEDIA = """
            Не удалось получить группу Одноклассников.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup;
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final OKDataCheck okDataCheck;
    private static final int rowsCount = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.SyncOkTg.getDescription()
    );

    public OkGroupDescription(String commandIdentifier, String description,
                              Map<Long, SocialMediaGroup> currentSocialMediaGroup,
                              Map<Long, AuthData> currentSocialMediaAccount,
                              OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentSocialMediaGroup = currentSocialMediaGroup;
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentSocialMediaGroup.get(chat.getId()) != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(OK_GROUP_DESCRIPTION,
                            okDataCheck.getOKGroupName(currentSocialMediaGroup.get(chat.getId()).getId(),
                            currentSocialMediaAccount.get(chat.getId()).getAccessToken())),
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
                    String.format(NOT_VALID_SOCIAL_MEDIA, State.MainMenu.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
