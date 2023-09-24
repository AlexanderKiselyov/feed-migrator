package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.util.IState;

import java.util.Collections;
import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public abstract class Command implements IBotCommand {
    public static final String USERNAME_NOT_FOUND = "Не удалось найти имя пользователя. Попробуйте еще раз.";
    public static final String GROUP_NAME_NOT_FOUND = "Не удалось найти название группы. Попробуйте еще раз.";
    public static final String USER_ID_NOT_FOUND = "Не удалось найти id пользователя. Попробуйте еще раз.";
    protected static final String YES_ANSWER = "Да";
    protected static final String NO_ANSWER = "Нет";
    protected static final String DELETE_MESSAGE = " Удалить";
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

    @Autowired
    private InlineKeyboard inlineKeyboard;

    @Autowired
    private ReplyKeyboard replyKeyboard;

    @Autowired
    ContextStorage contextStorage;

    public abstract IState state();

    protected abstract void doExecute(AbsSender absSender, User user, Chat chat, Context context);

    @Override
    public String getCommandIdentifier() {
        return state().getIdentifier();
    }

    @Override
    public String getDescription() {
        return state().getDescription();
    }

    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        execute(absSender, message, arguments);
    }

    public void execute(AbsSender absSender, Message message, String[] arguments) {
        Context context = contextStorage.getByMessage(message);
        context.resetCurrentState(state());
        doExecute(absSender, message.getFrom(), message.getChat(), context);
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
