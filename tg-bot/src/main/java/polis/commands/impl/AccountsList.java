package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.keyboards.callbacks.objects.AccountCallback;
import polis.keyboards.callbacks.parsers.AccountCallbackParser;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.util.Emojis;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountsList extends Command {
    private static final String ACCOUNTS_LIST_MSG = """
            Список Ваших аккаунтов.
            Чтобы выбрать аккаунт, нажмите на соответствующую кнопку.""";
    private static final String NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST_MSG = """
            Список аккаунтов пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private static final String ACCOUNT_INFO = "%s (%s)";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.AddGroup.getDescription());

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private AccountCallbackParser accountCallbackParser;

    public AccountsList(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.AccountsList.getIdentifier(), State.AccountsList.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<Account> accounts = accountsRepository.getAccountsForUser(chat.getId());

        if (accounts != null && !accounts.isEmpty()) {
            sendAnswerWithInlineKeyboard(
                    absSender,
                    chat.getId(),
                    ACCOUNTS_LIST_MSG,
                    accounts.size(),
                    getButtonsForAccounts(accounts),
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                String.format(NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST_MSG, State.AddGroup.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }

    private List<String> getButtonsForAccounts(List<Account> socialMediaAccounts) {
        List<String> buttons = new ArrayList<>(socialMediaAccounts.size() * 4);
        for (Account account : socialMediaAccounts) {
            String accountUsername = account.getUserFullName();
            String socialMediaName = account.getSocialMedia().getName();
            long accountId = account.getAccountId();
            buttons.add(String.format(ACCOUNT_INFO, accountUsername, socialMediaName));
            buttons.add(accountCallbackParser.toText(new AccountCallback(accountId, false, socialMediaName)));
            buttons.add(Emojis.TRASH + DELETE_MESSAGE);
            buttons.add(accountCallbackParser.toText(new AccountCallback(accountId, false, socialMediaName)));
        }
        return buttons;
    }
}
