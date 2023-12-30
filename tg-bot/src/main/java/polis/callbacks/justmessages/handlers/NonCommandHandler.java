package polis.callbacks.justmessages.handlers;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.util.AnswerPair;
import polis.commands.context.Context;
import polis.callbacks.justmessages.MessageCallbackHandler;
import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.justmessages.SomeMessage;

//TODO GET RID OF REMAINS OF THE NONCOMMAND class
public abstract class NonCommandHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {
    protected abstract AnswerPair nonCommandExecute(long chatId, String text, Context context);

    @Override
    protected void handleCallback(long userChatId, Message message, SomeMessage callback, Context context) throws TelegramApiException {
        AnswerPair answerPair = nonCommandExecute(userChatId, callback.text, context);
        sendAnswer(userChatId, getUserName(message), answerPair.getAnswer());
    }
}
