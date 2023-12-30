package polis.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.bot.MessageSender;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.keyboards.ReplyKeyboard;
import polis.util.IState;

import java.util.List;

public abstract class UtilCallbackHandler<CB extends Callback> implements CallbackHandler<CB> {
    @Lazy
    @Autowired
    protected ICommandRegistry commandRegistry;

    @Lazy
    @Autowired
    protected AbsSender sender;

    @Autowired
    protected MessageSender messageSender;

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

    protected void processNextCommand(IState state, Message message, String[] args) {
        IBotCommand command = commandRegistry.getRegisteredCommand(state.getIdentifier());
        command.processMessage(sender, message, args);
    }

    protected String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }

    protected void sendAnswer(long chatId, String username, String text, List<String> buttonsList) {
        messageSender.sendAnswer(chatId, username, text, buttonsList);
    }

    protected void deleteLastMessage(Message msg) throws TelegramApiException {
        messageSender.deleteLastMessage(msg);
    }
}
