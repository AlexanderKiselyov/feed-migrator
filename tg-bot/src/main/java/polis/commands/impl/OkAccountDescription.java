package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.data.domain.CurrentAccount;
import polis.data.repositories.CurrentAccountRepository;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.util.State;

import java.util.List;

@Component
public class OkAccountDescription extends Command {
    private static final String ACCOUNT_DESCRIPTION = """
            Выбран аккаунт в социальной сети Одноклассники с названием <b>%s</b>.""";
    private static final String NOT_VALID_ACCOUNT = """
            Невозможно получить информацию по текущему аккаунту.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS = List.of(State.AddOkGroup.getDescription());
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    public OkAccountDescription(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.OkAccountDescription.getIdentifier(), State.OkAccountDescription.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());

        boolean noCurrentAccountCondition = currentAccount == null;
        String text = noCurrentAccountCondition
                ? String.format(NOT_VALID_ACCOUNT, State.AddGroup.getIdentifier())
                : String.format(ACCOUNT_DESCRIPTION, currentAccount.getUserFullName());
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                text,
                ROWS_COUNT,
                noCurrentAccountCondition ? KEYBOARD_COMMANDS_IN_ERROR_CASE : KEYBOARD_COMMANDS,
                loggingInfo(user.getUserName()));
    }
}
