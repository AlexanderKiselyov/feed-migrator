package polis.keyboards.callbacks.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.data.domain.CurrentState;
import polis.data.repositories.CurrentStateRepository;
import polis.keyboards.callbacks.CallbackParser;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GoBackCallback;
import polis.keyboards.callbacks.parsers.GoBackCallbackParser;
import polis.util.IState;
import polis.util.State;

@Component
public class GoBackCallbackHandler extends ACallbackHandler<GoBackCallback> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoBackCallbackHandler.class);

    @Autowired
    private CurrentStateRepository currentStateRepository;

    {
        callbackParser = new GoBackCallbackParser();
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GO_BACK;
    }

    @Override
    protected CallbackParser<GoBackCallback> callbackParser() {
        return callbackParser;
    }

    @Override
    public void handleCallback(long userChatId, Message message, GoBackCallback callback) throws TelegramApiException {
        CurrentState currentState = currentStateRepository.getCurrentState(userChatId);
        if (currentState != null) {
            IState previousState = State.getPrevState(currentState.getState());
            if (previousState == null) {
                LOGGER.error("Previous state = null, tmp state = {}", currentStateRepository
                        .getCurrentState(userChatId).getState().getIdentifier());
                return;
            }
            currentStateRepository.insertCurrentState(new CurrentState(userChatId, previousState.getIdentifier()));
            deleteLastMessage(message);
            getRegisteredCommand(previousState.getIdentifier()).processMessage(sender, message, null);
        }
    }
}
