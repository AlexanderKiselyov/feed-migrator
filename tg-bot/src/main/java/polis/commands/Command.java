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
    public static final String USER_ID_NOT_FOUND = "Не удалось найти id пользователя. Попробуйте еще раз.";
    public static final String USERNAME_NOT_FOUND = "Не удалось найти имя пользователя. Попробуйте еще раз.";
    public static final String GROUP_NAME_NOT_FOUND = "Не удалось найти название группы. Попробуйте еще раз.";
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

    public Command(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    private void setAndSendMessage(AbsSender absSender, Long chatId, String commandName, String userName, String text,
                                   SendMessage message) {
        message.setChatId(chatId.toString());
        message.setParseMode(ParseMode.HTML);
        message.setText(text);
        message.disableWebPagePreview();

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error(String.format("Cannot execute command %s of user %s: %s", commandName, userName,
                    e.getMessage()));
        }
    }

    void sendAnswerWithInlineKeyboard(AbsSender absSender, Long chatId, String commandName, String userName,
                                      String text, int rowsCount, List<String> inlineKeyboardCommands) {
        SendMessage message = InlineKeyboard.INSTANCE.createSendMessage(chatId, text, rowsCount,
                inlineKeyboardCommands);
        setAndSendMessage(absSender, chatId, commandName, userName, text, message);
    }

    void sendAnswerWithReplyKeyboard(AbsSender absSender, Long chatId, String commandName, String userName, String text,
                                     int rowsCount, List<String> commandsList) {
        SendMessage message = ReplyKeyboard.INSTANCE.createSendMessage(chatId, text, rowsCount, commandsList);
        setAndSendMessage(absSender, chatId, commandName, userName, text, message);
    }

    void sendAnswerWithReplyKeyboardAndBackButton(AbsSender absSender, Long chatId, String commandName, String userName,
                                                  String text, int rowsCount, List<String> commandsList) {
        SendMessage message = ReplyKeyboard.INSTANCE.createSendMessage(chatId, text, rowsCount, commandsList,
                GO_BACK_BUTTON_TEXT);
        setAndSendMessage(absSender, chatId, commandName, userName, text, message);
    }

    void sendAnswerWithInlineKeyboardAndBackButton(AbsSender absSender, Long chatId, String commandName,
                                                   String userName, String textReply, String textInline, int rowsCount,
                                                   List<String> inlineKeyboardCommands) {
        sendAnswerWithOnlyBackButton(absSender, chatId, commandName, userName, textReply);
        sendAnswerWithInlineKeyboard(absSender, chatId, commandName, userName, textInline, rowsCount,
                inlineKeyboardCommands);
    }

    void sendAnswerWithOnlyBackButton(AbsSender absSender, Long chatId, String commandName, String userName,
                                      String text) {
        SendMessage message = ReplyKeyboard.INSTANCE.createSendMessage(chatId, text, 0, Collections.emptyList(),
                GO_BACK_BUTTON_TEXT);
        setAndSendMessage(absSender, chatId, commandName, userName, text, message);
    }
}
