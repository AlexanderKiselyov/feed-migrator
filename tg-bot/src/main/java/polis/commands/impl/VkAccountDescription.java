package polis.commands.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class VkAccountDescription extends Command {
    private static final String ACCOUNT_DESCRIPTION = """
            Выбран аккаунт в социальной сети ВКонтакте с названием <b>%s</b>.""";
    private static final String NOT_VALID_ACCOUNT = """
            Невозможно получить информацию по текущему аккаунту.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";

    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS = List.of(State.AddVkGroup.getDescription());
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.AddVkGroup.getDescription());

    @Override
    public IState state() {
        return State.VkAccountDescription;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        Account currentAccount = context.currentAccount();

        boolean noErrorCondition = currentAccount != null;
        String text = noErrorCondition ? String.format(ACCOUNT_DESCRIPTION, currentAccount.getUserFullName())
                : String.format(NOT_VALID_ACCOUNT, State.AddGroup.getIdentifier());
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                text,
                ROWS_COUNT,
                noErrorCondition ? KEYBOARD_COMMANDS : KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }
}
