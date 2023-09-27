package polis.callbacks.messages.handlers;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.NonCommand;
import polis.commands.context.Context;
import polis.callbacks.messages.MessageCallbackHandler;
import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.messages.SomeMessage;

//TODO REMOVE SMELLY REMAINS OF THE NONCOMMAND class
//TODO then this abraction will be reorganized into a slightly different one
public abstract class NonCommandHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {
    protected abstract NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context);

    @Override
    protected void handleCallback(long userChatId, Message message, SomeMessage callback, Context context) throws TelegramApiException {
        NonCommand.AnswerPair answerPair = nonCommandExecute(userChatId, callback.text, context);
        sendAnswer(userChatId, getUserName(message), answerPair.getAnswer());
    }
}
