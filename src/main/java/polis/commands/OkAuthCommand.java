package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.authorization.OkAuthorization;

import java.net.URISyntaxException;

public class OkAuthCommand extends Command {
    private final Logger logger = LoggerFactory.getLogger(OkAuthCommand.class);

    public OkAuthCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String userName = (user.getUserName() != null) ? user.getUserName() :
                String.format("%s %s", user.getLastName(), user.getFirstName());
        try {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), userName,
                    String.format("""
                            Для авторизации в социальной сети Одноклассники перейдите по ссылке:
                            %s
                            После авторизации скопируйте код авторизации из адресной строки и отправьте его в этот диалог.""",
                            OkAuthorization.formAuthorizationUrl()));
        } catch (URISyntaxException e) {
            logger.error(String.format("Cannot form link: %s", e));
        }
    }
}
