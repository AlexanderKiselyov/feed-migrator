package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.AuthData;
import polis.util.State;

import java.util.Map;

public class OkAccountDescription extends Command {
    private static final String ACCOUNT_DESCRIPTION = """
            Выбран аккаунт в социальной сети Одноклассники с названием <b>%s</b>.""";
    private static final String NOT_VALID_ACCOUNT = """
            Невозможно получить информацию по текущему аккаунту.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, AuthData> currentSocialMediaAccount;

    public OkAccountDescription(String commandIdentifier, String description,
                                Map<Long, AuthData> currentSocialMediaAccount) {
        super(commandIdentifier, description);
        this.currentSocialMediaAccount = currentSocialMediaAccount;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentSocialMediaAccount.containsKey(chat.getId())) {
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(ACCOUNT_DESCRIPTION, currentSocialMediaAccount.get(chat.getId()).getUsername()),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        } else {
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_ACCOUNT, State.AddGroup.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        }
    }
}
