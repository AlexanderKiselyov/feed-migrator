package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.repositories.CurrentAccountRepository;
import polis.ok.OKDataCheck;
import polis.util.State;

import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class OkAccountDescription extends Command {
    private static final String ACCOUNT_DESCRIPTION = """
            Выбран аккаунт в социальной сети Одноклассники с названием <b>%s</b>.""";
    private static final String NOT_VALID_ACCOUNT = """
            Невозможно получить информацию по текущему аккаунту.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final CurrentAccountRepository currentAccountRepository;
    private final OKDataCheck okDataCheck;
    private static final int rowsCount = 2;
    private static final List<String> commandsForKeyboard = List.of(
            State.AddOkGroup.getDescription()
    );

    public OkAccountDescription(String commandIdentifier, String description,
                                CurrentAccountRepository currentAccountRepository,
                                OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentAccountRepository = currentAccountRepository;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        if (currentAccount != null) {
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(ACCOUNT_DESCRIPTION,
                            okDataCheck.getOKUsername(currentAccount.getAccessToken())),
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
