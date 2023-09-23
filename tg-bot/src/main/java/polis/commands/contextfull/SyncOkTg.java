package polis.commands.contextfull;

import org.springframework.stereotype.Component;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.util.State;

@Component
public class SyncOkTg extends SyncGroupWithChannel {

    public SyncOkTg(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.SyncOkTg.getIdentifier(), State.SyncOkTg.getDescription(), inlineKeyboard, replyKeyboard);
    }
}
