package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.Keyboard;

public class StartCommand extends Command {
    private static final String START_ANSWER = "Давайте начнём! Выберите /okauth, чтобы авторизоваться "
            + "в социальной сети Одноклассники.";
    private final Logger logger = LoggerFactory.getLogger(StartCommand.class);

    public StartCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage sendMessage = Keyboard.createSendMessage(chat.getId(), START_ANSWER);
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot send message: %s", e));
        }
    }
}
