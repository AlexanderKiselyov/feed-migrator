package polis.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.bot.Bot;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.keyboards.ReplyKeyboard;
import polis.util.IState;

import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public abstract class UtilCallbackHandler<CB extends Callback> implements CallbackHandler<CB> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilCallbackHandler.class);
    @Lazy
    @Autowired
    protected ICommandRegistry commandRegistry;

    @Lazy
    @Autowired
    protected AbsSender sender;

    @Autowired
    protected ContextStorage contextStorage;

    @Autowired
    protected ReplyKeyboard replyKeyboard;

    protected abstract void handleCallback(long userChatId, Message message, CB callback, Context context) throws TelegramApiException;

    @Override
    public void handleCallback(Message message, CB callback) throws TelegramApiException {
        Context context = contextStorage.getByMessage(message);
        Long userChatId = message.getChatId();
        handleCallback(userChatId, message, callback, context);
    }

    protected void deleteLastMessage(Message msg) throws TelegramApiException {
        long chatId = msg.getChatId();
        DeleteMessage lastMessage = new DeleteMessage();
        lastMessage.setChatId(chatId);
        lastMessage.setMessageId(msg.getMessageId());
        sender.execute(lastMessage);
    }

    protected void processNextCommand(IState state, AbsSender sender, Message message, String[] args) {
        IBotCommand command = commandRegistry.getRegisteredCommand(state.getIdentifier());
        command.processMessage(sender, message, args);
    }

    protected void sendAnswer(Long chatId, String userName, String text) {
        SendMessage answer = new SendMessage();

        if (Bot.BUTTONS_TEXT_MAP.containsKey(text)) {
            List<String> commandsList = Bot.BUTTONS_TEXT_MAP.get(text);
            answer = replyKeyboard.createSendMessage(chatId, text, commandsList.size(), commandsList,
                    GO_BACK_BUTTON_TEXT);
        } else {
            answer.setParseMode(ParseMode.HTML);
            answer.disableWebPagePreview();
        }
        answer.setChatId(chatId.toString());
        answer.setText(text);

        try {
            sender.execute(answer);
        } catch (TelegramApiException e) {
            if (userName != null) {
                LOGGER.error(String.format("Cannot execute command of user %s: %s", userName, e.getMessage()));
            } else {
                LOGGER.error(String.format("Cannot execute command: %s", e.getMessage()));
            }
        }
    }

    protected String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }
}
