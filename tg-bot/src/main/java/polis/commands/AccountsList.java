package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.util.Emojis;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountsList extends Command {
    private static final String ACCOUNTS_LIST_MSG = "Список Ваших аккаунтов.";
    private static final String ACCOUNTS_LIST_INLINE_MSG = "Чтобы выбрать аккаунт, нажмите на соответствующую кнопку.";
    private static final String NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST_MSG = """
            Список аккаунтов пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboardInErrorCase = List.of(State.AddGroup.getDescription());

    @Autowired
    private AccountsRepository accountsRepository;

    public AccountsList() {
        super(State.AccountsList.getIdentifier(), State.AccountsList.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<Account> accounts = accountsRepository.getAccountsForUser(chat.getId());

        if (accounts != null && !accounts.isEmpty()) {
            sendAnswerWithInlineKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    ACCOUNTS_LIST_MSG,
                    ACCOUNTS_LIST_INLINE_MSG,
                    accounts.size(),
                    getButtonsForAccounts(accounts));
            return;
        }
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                String.format(NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST_MSG, State.AddGroup.getIdentifier()),
                ROWS_COUNT,
                commandsForKeyboardInErrorCase);
    }

    private List<String> getButtonsForAccounts(List<Account> socialMediaAccounts) {
        List<String> buttons = new ArrayList<>(socialMediaAccounts.size() * 4);
        for (Account account : socialMediaAccounts) {
            String accountUsername = account.getUserFullName();
            String socialMediaName = account.getSocialMedia().getName();
            long accountId = account.getAccountId();

            buttons.add(String.format("%s (%s)", accountUsername, socialMediaName));
            buttons.add(String.format("account %d 0 %s", accountId, socialMediaName));
            buttons.add(Emojis.TRASH + " Удалить");
            buttons.add(String.format("account %d 1 %s", accountId, socialMediaName));
        }
        return buttons;
    }
}
