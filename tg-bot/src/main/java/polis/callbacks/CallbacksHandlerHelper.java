package polis.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.typed.TypedCallbackHandler;
import polis.callbacks.justmessages.MessageCallbackHandler;
import polis.callbacks.typed.parsers.ACallbackParser;
import polis.callbacks.justmessages.SomeMessage;
import polis.util.IState;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CallbacksHandlerHelper {
    private static final Logger logger = LoggerFactory.getLogger(CallbacksHandlerHelper.class);

    private final Map<String, TypedCallbackHandler<? extends Callback>> handlersByType;
    private final Map<String, MessageCallbackHandler> handlersByStateIdentifier;

    @Autowired
    public CallbacksHandlerHelper(
            List<TypedCallbackHandler<?>> handlers,
            List<MessageCallbackHandler> replyKeyboardCallbackHandlers
    ) {
        this.handlersByType = handlers.stream().collect(Collectors.toMap(
                handler -> handler.callbackType().stringKey,
                Function.identity()
        ));
        this.handlersByStateIdentifier = replyKeyboardCallbackHandlers.stream().collect(Collectors.toMap(
                handler -> handler.state().getIdentifier(),
                Function.identity()
        ));
    }

    public void handleCallback(String callbackString, Message message) throws TelegramApiException {
        ACallbackParser.CallbackTypeAndData typeAndData = ACallbackParser.parseTypeAndData(callbackString);
        TypedCallbackHandler<? extends Callback> handler = handlersByType.get(typeAndData.type());
        if (handler == null) {
            logger.error("Cannot find handler for callback with type %s. Callback: %s"
                    .formatted(typeAndData.type(), typeAndData.data())
            );
            throw new IllegalArgumentException("Cannot find handler for callback with type " + typeAndData.type());
        }
        handler.handleCallback(message, typeAndData.data());
    }

    public void handleMessageCallback(String text, IState state, Message message) throws TelegramApiException {
        MessageCallbackHandler handler = handlersByStateIdentifier.get(state.getIdentifier());
        if (handler == null) {
            throw new IllegalArgumentException("Cannot find handler for state " + state);
        }
        handler.handleCallback(message, new SomeMessage(text));
    }
}
