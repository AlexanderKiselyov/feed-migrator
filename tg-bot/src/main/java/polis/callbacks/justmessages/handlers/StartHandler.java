package polis.callbacks.justmessages.handlers;

import polis.util.AnswerPair;
import polis.commands.context.Context;
import polis.util.IState;
import polis.util.State;

//FIXME what is it?
public class StartHandler extends NonCommandHandler {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";

    @Override
    public IState state() {
        return State.Start;
    }

    @Override
    protected AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        return new AnswerPair(START_STATE_ANSWER, true);
    }
}
