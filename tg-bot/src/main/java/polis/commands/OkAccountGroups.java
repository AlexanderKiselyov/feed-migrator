package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.AuthData;
import polis.util.State;

import java.util.List;
import java.util.Map;

import static polis.bot.Bot.NO_CALLBACK_TEXT;
import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class OkAccountGroups extends Command {
    // TODO: Добавить текст, чтобы отправлять два сообщения (для двух разных клавиатур)
    private static final String OK_ACCOUNT_GROUPS = """
            Список групп OK аккаунта <b>%s</b>:""";
    private static final String OK_ACCOUNT_GROUPS_INLINE = "TODO сообщение";
    private static final String NOT_VALID_SOCIAL_MEDIA_GROUPS_LIST = """
            Список групп ОК аккаунта пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, AuthData> currentSocialMediaAccount;

    public OkAccountGroups(String commandIdentifier, String description,
                           Map<Long, AuthData> currentSocialMediaAccount) {
        super(commandIdentifier, description);
        this.currentSocialMediaAccount = currentSocialMediaAccount;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<String> groupLinks = currentSocialMediaAccount.get(chat.getId()).getGroupLinks();
        if (currentSocialMediaAccount.containsKey(chat.getId()) && groupLinks != null && groupLinks.size() != 0) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(OK_ACCOUNT_GROUPS, currentSocialMediaAccount.get(chat.getId()).getUsername()),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    OK_ACCOUNT_GROUPS_INLINE,
                    groupLinks.size(),
                    commandsForKeyboard,
                    getGroupsArray(groupLinks));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_SOCIAL_MEDIA_GROUPS_LIST, State.MainMenu.getIdentifier()),
                    1,
                    List.of(State.MainMenu.getDescription()), // TODO: Ошибка при возврате в текущий аккаунт
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getGroupsArray(List<String> groupLinks) {
        String[] buttons = new String[groupLinks.size() * 2];
        for (int i = 0; i < groupLinks.size(); i += 2) {
            buttons[i] = groupLinks.get(i);
            buttons[i + 1] = NO_CALLBACK_TEXT;
        }

        return buttons;
    }
}
