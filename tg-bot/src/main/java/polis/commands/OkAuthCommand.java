package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.ok.api.OkAuthorizator;

import java.net.URISyntaxException;
import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class OkAuthCommand extends Command {
    private static final String OK_AUTH_ANSWER = """
                    Для авторизации в социальной сети Одноклассники перейдите по ссылке:
                    %s
                    После авторизации скопируйте код авторизации из адресной строки и отправьте его в этот диалог.""";
    private final Logger logger = LoggerFactory.getLogger(OkAuthCommand.class);
    private static final int rowsCount = 0;
    private static final List<String> commandsForKeyboard = List.of(
    );

    public OkAuthCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            String messageText = String.format(OK_AUTH_ANSWER, OkAuthorizator.formAuthorizationUrl());
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), messageText,
                    rowsCount, commandsForKeyboard, GO_BACK_BUTTON_TEXT);
        } catch (URISyntaxException e) {
            logger.error(String.format("Cannot form link: %s", e));
        }
    }
}
