package polis.commands.contextfull;

import org.springframework.stereotype.Component;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.util.State;

@Component
public class SyncVkTg extends SyncGroupWithChannel {

    public SyncVkTg(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.SyncVkTg.getIdentifier(), State.SyncVkTg.getDescription(), inlineKeyboard, replyKeyboard);
    }
}
