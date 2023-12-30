package polis.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.util.AnswerPair;
import polis.commands.context.Context;
import polis.datacheck.VkDataCheck;
import polis.util.IState;
import polis.util.State;

@Component
public class AddVkAccountAccessTokenHandler extends NonCommandHandler {

    @Autowired
    private VkDataCheck vkDataCheck;

    @Override
    public IState state() {
        return State.AddVkAccount;
    }

    @Override
    protected AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        return vkDataCheck.getVkAccessToken(text, chatId);
    }
}
