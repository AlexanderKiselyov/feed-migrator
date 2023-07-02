package polis.keyboards.callbacks.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.keyboards.callbacks.CallbackHandler;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.objects.Callback;

@Component
public abstract class ACallbackHandler<CB extends Callback> implements CallbackHandler<CB> {
    @Autowired
    protected ICommandRegistry commandRegistry;
    @Autowired
    protected AbsSender sender;

    protected CallbackParser<CB> callbackParser;

    protected abstract CallbackParser<CB> callbackParser();

    protected abstract void handleCallback(long userChatId, Message message, CB callback) throws TelegramApiException;

    @Override
    public void handleCallback(Message message, String callbackData) throws TelegramApiException {
        CallbackParser<CB> parser = callbackParser();
        CB callback = parser.fromText(callbackData);
        handleCallback(message.getChatId(), message, callback);
    }

    protected void deleteLastMessage(Message msg) throws TelegramApiException {
        long chatId = msg.getChatId();
        DeleteMessage lastMessage = new DeleteMessage();
        lastMessage.setChatId(chatId);
        lastMessage.setMessageId(msg.getMessageId());
        sender.execute(lastMessage);
    }

    protected IBotCommand getRegisteredCommand(String identifier) {
        return commandRegistry.getRegisteredCommand(identifier);
    }
}
