package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.AuthData;
import polis.util.State;

import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class AccountsList extends Command {
    // TODO: Добавить текст, чтобы отправлять два сообщения (для двух разных клавиатур)
    private static final String ACCOUNTS_LIST = """
            Список аккаунтов:""";
    private static final String ACCOUNTS_LIST_INLINE = "TODO сообщение";
    private static final String NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST = """
            Список аккаунтов пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, List<AuthData>> socialMediaAccounts;

    public AccountsList(String commandIdentifier, String description, Map<Long, List<AuthData>> socialMediaAccounts) {
        super(commandIdentifier, description);
        this.socialMediaAccounts = socialMediaAccounts;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<AuthData> authDataList = socialMediaAccounts.get(chat.getId());
        if (socialMediaAccounts.containsKey(chat.getId()) && authDataList.size() != 0) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    ACCOUNTS_LIST,
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    ACCOUNTS_LIST_INLINE,
                    authDataList.size(),
                    commandsForKeyboard,
                    getAccountsArray(authDataList));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST, State.AddGroup.getIdentifier()),
                    1,
                    List.of(State.AddGroup.getDescription()),
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    // TODO: рефакторинг и перенос функционала inline-клавиатуры в класс InlineKeyboard в процессе
    private String[] getAccountsArray(List<AuthData> socialMediaAccounts) {
        String[] buttons = new String[socialMediaAccounts.size() * 2];
        for (int i = 0; i < socialMediaAccounts.size(); i++) {
            int tmpIndex = i * 2;
            buttons[tmpIndex] = String.format("%s (%s)", socialMediaAccounts.get(i).getUsername(),
                    socialMediaAccounts.get(i).getSocialMedia().getName());
            buttons[tmpIndex + 1] = String.format("account %s %s %s",
                    socialMediaAccounts.get(i).getSocialMedia().getName(),
                    socialMediaAccounts.get(i).getAccessToken(),
                    socialMediaAccounts.get(i).getUsername());
        }

        return buttons;
    }
}
