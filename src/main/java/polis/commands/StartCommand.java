package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.Keyboard;
import polis.util.State;

public class StartCommand extends Command {
    private static final String COMMAND_SYMBOL = "/";
    private static final String STATE_DELIMITER = " - ";
    private static final String STRING_DELIMITER = "\n";
    private String startAnswer = """
            Давайте начнём!
            С помощью бота Вы можете синхронизировать Ваш Telegram-канал с группой в Одноклассники
                            
            Команды:
            """;
    private final Logger logger = LoggerFactory.getLogger(StartCommand.class);

    public StartCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
        StringBuilder stringBuilder = new StringBuilder(startAnswer);
        for (State state : State.values()) {
            stringBuilder
                    .append(COMMAND_SYMBOL)
                    .append(state.getIdentifier())
                    .append(STATE_DELIMITER)
                    .append(state.getDescription())
                    .append(STRING_DELIMITER);
        }
        startAnswer = stringBuilder.toString();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage sendMessage = Keyboard.createSendMessage(chat.getId(), startAnswer);
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot send message: %s", e));
        }
    }
}
