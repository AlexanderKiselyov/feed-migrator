package polis.commands.contextfull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.ok.api.OkAuthorizator;
import polis.util.State;

import java.net.URISyntaxException;

@Component
public class AddOkAccount extends Command {
    private static final String OK_AUTH_ANSWER_MSG = """
                    Для авторизации в социальной сети Одноклассники перейдите по ссылке:
                    %s
                    После авторизации скопируйте код авторизации из адресной строки и отправьте его в этот диалог.""";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddOkAccount.class);

    public AddOkAccount(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.AddOkAccount.getIdentifier(), State.AddOkAccount.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            String messageText = String.format(OK_AUTH_ANSWER_MSG, OkAuthorizator.formAuthorizationUrl());
            sendAnswerWithOnlyBackButton(
                    absSender,
                    chat.getId(),
                    messageText,
                    loggingInfo(user.getUserName()));
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Cannot form link: %s", e));
        }
    }
}
