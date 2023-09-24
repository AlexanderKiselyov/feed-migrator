package polis.keyboards.callbacks.handlers.replykeyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.NonCommand;
import polis.commands.context.Context;
import polis.datacheck.OkDataCheck;
import polis.util.IState;
import polis.util.State;

@Component
public class GetOkAuthCodeHandler extends NonCommandHandler {
    @Autowired
    private OkDataCheck okDataCheck;

    @Override
    public IState state() {
        return State.AddOkAccount;
    }

    @Override
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        return okDataCheck.getOKAuthCode(text, chatId);
    }
}
