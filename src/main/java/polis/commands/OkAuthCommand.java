package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.authorization.OkAuthorization;
import polis.keyboards.Keyboard;

import java.net.URISyntaxException;
import java.util.Properties;
import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class OkAuthCommand extends Command {
    private static final String OK_AUTH_ANSWER = """
                    Для авторизации в социальной сети Одноклассники перейдите по ссылке:
                    %s
                    После авторизации скопируйте код авторизации из адресной строки и отправьте его в этот диалог.""";
    private final Logger logger = LoggerFactory.getLogger(OkAuthCommand.class);
    private final Properties properties;

    public OkAuthCommand(String commandIdentifier, String description, Properties properties) {
        super(commandIdentifier, description);
        this.properties = properties;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            String messageText = String.format(OK_AUTH_ANSWER,
                    OkAuthorization.formAuthorizationUrl(properties.getProperty("okapp.id"),
                            properties.getProperty("okapp.redirect_uri")));
            SendMessage sendMessage = Keyboard.createSendMessage(chat.getId(), messageText, GO_BACK_BUTTON_TEXT);
            absSender.execute(sendMessage);
        } catch (URISyntaxException e) {
            logger.error(String.format("Cannot form link: %s", e));
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot send message: %s", e));
        }
    }
}
