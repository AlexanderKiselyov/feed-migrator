package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.repositories.CurrentAccountRepository;
import polis.dataCheck.DataCheck;
import polis.util.State;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class OkAccountDescription extends Command {
    private static final String ACCOUNT_DESCRIPTION = """
            Выбран аккаунт в социальной сети Одноклассники с названием <b>%s</b>.""";
    private static final String NOT_VALID_ACCOUNT = """
            Невозможно получить информацию по текущему аккаунту.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private DataCheck dataCheck;

    private static final int rowsCount = 2;
    private static final List<String> commandsForKeyboard = List.of(
            State.AddOkGroup.getDescription()
    );

    public OkAccountDescription() {
        super(State.OkAccountDescription.getIdentifier(), State.OkAccountDescription.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());

        if (currentAccount != null) {
            String username = dataCheck.getOKUsername(currentAccount.getAccessToken());

            if (Objects.equals(username, "")) {
                sendAnswer(absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        USERNAME_NOT_FOUND,
                        rowsCount,
                        commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
                return;
            }

            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(ACCOUNT_DESCRIPTION, username),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_ACCOUNT, State.AddGroup.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
