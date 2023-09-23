package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;

import java.util.Collections;
import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public abstract class Command extends BotCommand {
    public static final String USERNAME_NOT_FOUND = "Не удалось найти имя пользователя. Попробуйте еще раз.";
    public static final String GROUP_NAME_NOT_FOUND = "Не удалось найти название группы. Попробуйте еще раз.";
    public static final String USER_ID_NOT_FOUND = "Не удалось найти id пользователя. Попробуйте еще раз.";
    protected static final String YES_ANSWER = "Да";
    protected static final String NO_ANSWER = "Нет";
    protected static final String DELETE_MESSAGE = " Удалить";
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

    private final InlineKeyboard inlineKeyboard;
    private final ReplyKeyboard replyKeyboard;

    public Command(
            String commandIdentifier,
            String description,
            InlineKeyboard inlineKeyboard,
            ReplyKeyboard replyKeyboard
    ) {
        super(commandIdentifier, description);
        this.inlineKeyboard = inlineKeyboard;
        this.replyKeyboard = replyKeyboard;
    }

    private void setAndSendMessage(AbsSender absSender, Long chatId, String text, SendMessage message,
                                   LoggingInfo loggingInfo) {
        message.setChatId(chatId.toString());
        message.setParseMode(ParseMode.HTML);
        message.setText(text);
        message.disableWebPagePreview();

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error(String.format("Cannot execute command %s of user %s: %s", loggingInfo.commandIdentifier,
                    loggingInfo.userName, e.getMessage()));
        }
    }

    protected void sendAnswerWithInlineKeyboard(AbsSender absSender, Long chatId, String text, int rowsCount,
                                      List<String> inlineKeyboardCommands, LoggingInfo loggingInfo) {
        SendMessage message = inlineKeyboard.createSendMessage(chatId, text, rowsCount,
                inlineKeyboardCommands);
        setAndSendMessage(absSender, chatId, text, message, loggingInfo);
    }

    protected void sendAnswerWithReplyKeyboard(AbsSender absSender, Long chatId, String text, int rowsCount,
                                     List<String> commandsList, LoggingInfo loggingInfo) {
        SendMessage message = replyKeyboard.createSendMessage(chatId, text, rowsCount, commandsList);
        setAndSendMessage(absSender, chatId, text, message, loggingInfo);
    }

    protected void sendAnswerWithReplyKeyboardAndBackButton(AbsSender absSender, Long chatId, String text, int rowsCount,
                                                  List<String> commandsList, LoggingInfo loggingInfo) {
        SendMessage message = replyKeyboard.createSendMessage(chatId, text, rowsCount, commandsList,
                GO_BACK_BUTTON_TEXT);
        setAndSendMessage(absSender, chatId, text, message, loggingInfo);
    }

    protected void sendAnswerWithOnlyBackButton(AbsSender absSender, Long chatId, String text, LoggingInfo loggingInfo) {
        SendMessage message = replyKeyboard.createSendMessage(chatId, text, 0, Collections.emptyList(),
                GO_BACK_BUTTON_TEXT);
        setAndSendMessage(absSender, chatId, text, message, loggingInfo);
    }

    protected LoggingInfo loggingInfo(String userName) {
        return new LoggingInfo(userName, getCommandIdentifier());
    }

    private record LoggingInfo(String userName, String commandIdentifier) {
    }
}
