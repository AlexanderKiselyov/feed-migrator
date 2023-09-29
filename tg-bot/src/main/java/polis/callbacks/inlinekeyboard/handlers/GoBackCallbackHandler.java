package polis.callbacks.inlinekeyboard.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.objects.GoBackCallback;
import polis.commands.context.Context;
import polis.callbacks.inlinekeyboard.parsers.GoBackCallbackParser;
import polis.util.IState;
import polis.util.State;

@Component
public class GoBackCallbackHandler extends AInlineKeyboardCBHandler<GoBackCallback> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoBackCallbackHandler.class);

    public GoBackCallbackHandler(GoBackCallbackParser callbackParser) {
        this.callbackParser = callbackParser;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GO_BACK;
    }

    @Override
    public void handleCallback(long userChatId, Message message, GoBackCallback callback, Context context) throws TelegramApiException {
        IState currentState = context.currentState();
        if (currentState != null) {
            IState previousState = State.getPrevState(currentState);
            if (previousState == null) {
                LOGGER.error("Previous state = null, tmp state = {}", currentState.getIdentifier());
                return;
            }
            deleteLastMessage(message);
            processNextCommand(previousState, sender, message, null);
        }
    }
}
