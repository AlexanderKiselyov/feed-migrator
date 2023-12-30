package polis.callbacks.justmessages.handlers;

import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.justmessages.MessageCallbackHandler;
import polis.callbacks.justmessages.SomeMessage;
import polis.commands.context.Context;
import polis.util.AnswerPair;

//Look for examples in MessageCallbackHandler's that do not inherit this class
@Deprecated(since = "The logic tied to class AnswerPair most likely needs refactoring")
public abstract class NonCommandHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {
    protected abstract AnswerPair nonCommandExecute(long chatId, String text, Context context);
}
