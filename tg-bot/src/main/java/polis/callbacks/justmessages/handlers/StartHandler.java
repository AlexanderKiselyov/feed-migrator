package polis.callbacks.justmessages.handlers;

import polis.commands.NonCommand;
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
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        return new NonCommand.AnswerPair(START_STATE_ANSWER, true);
    }
}
