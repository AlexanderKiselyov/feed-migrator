package polis.keyboards.callbacks.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.keyboards.callbacks.CallbackHandler;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.objects.Callback;
import polis.util.IState;

@Component
public abstract class ACallbackHandler<CB extends Callback> implements CallbackHandler<CB> {

    @Lazy
    @Autowired
    protected ICommandRegistry commandRegistry;

    @Lazy
    @Autowired
    protected AbsSender sender;

    @Autowired
    private ContextStorage contextStorage;

    protected CallbackParser<CB> callbackParser;

    protected abstract CallbackParser<CB> callbackParser();

    protected abstract void handleCallback(long userChatId, Message message, CB callback, Context context) throws TelegramApiException;

    @Override
    public void handleCallback(Message message, String callbackData) throws TelegramApiException {
        CallbackParser<CB> parser = callbackParser();
        CB callback = parser.fromText(callbackData);
        Long userChatId = message.getChatId();
        Context context = contextStorage.getByMessage(message);
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
}
