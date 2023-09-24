package polis.keyboards.callbacks.handlers.replykeyboard;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.NonCommand;
import polis.commands.context.Context;
import polis.keyboards.callbacks.handlers.ReplyKeyboardCallbackHandler;
import polis.keyboards.callbacks.handlers.UtilCallbackHandler;
import polis.keyboards.callbacks.objects.ReplyKeyboardCallback;

//TODO REMOVE SMELLY REMAINS OF THE NONCOMMAND class
//TODO then this abraction will be reorganized into a slightly different one
public abstract class NonCommandHandler extends UtilCallbackHandler<ReplyKeyboardCallback> implements ReplyKeyboardCallbackHandler {
    protected abstract NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context);

    @Override
    protected void handleCallback(long userChatId, Message message, ReplyKeyboardCallback callback, Context context) throws TelegramApiException {
        NonCommand.AnswerPair answerPair = nonCommandExecute(userChatId, callback.text, context);
        sendAnswer(userChatId, getUserName(message), answerPair.getAnswer());
    }
}
