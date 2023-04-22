package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.datacheck.DataCheck;
import polis.util.State;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class AccountsList extends Command {
    private static final String ACCOUNTS_LIST = """
            Список аккаунтов:""";
    private static final String ACCOUNTS_LIST_INLINE = "Чтобы выбрать аккаунт, нажмите на соответствующую кнопку.";
    private static final String NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST = """
            Список аккаунтов пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private DataCheck dataCheck;

    public AccountsList() {
        super(State.AccountsList.getIdentifier(), State.AccountsList.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<Account> accounts = accountsRepository.getAccountsForUser(chat.getId());

        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                if (Objects.equals(dataCheck.getOKUsername(account.getAccessToken()), "")) {
                    sendAnswer(
                            absSender,
                            chat.getId(),
                            this.getCommandIdentifier(),
                            user.getUserName(),
                            USERNAME_NOT_FOUND,
                            1,
                            List.of(State.AddGroup.getDescription()),
                            null,
                            GO_BACK_BUTTON_TEXT);
                    return;
                }
            }

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
                    accounts.size(),
                    commandsForKeyboard,
                    getAccountsArray(accounts));
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

    private String[] getAccountsArray(List<Account> socialMediaAccounts) {
        String[] buttons = new String[socialMediaAccounts.size() * 2];
        for (int i = 0; i < socialMediaAccounts.size(); i++) {
            int tmpIndex = i * 2;
            buttons[tmpIndex] = String.format("%s (%s)",
                    dataCheck.getOKUsername(socialMediaAccounts.get(i).getAccessToken()),
                    socialMediaAccounts.get(i).getSocialMedia());
            buttons[tmpIndex + 1] = String.format("account %d", socialMediaAccounts.get(i).getAccountId());
        }

        return buttons;
    }
}
