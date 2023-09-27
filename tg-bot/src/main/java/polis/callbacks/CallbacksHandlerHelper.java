package polis.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.inlinekeyboard.InlineKeyboardCallbackHandler;
import polis.callbacks.messages.MessageCallbackHandler;
import polis.callbacks.inlinekeyboard.parsers.ACallbackParser;
import polis.callbacks.messages.SomeMessage;
import polis.util.IState;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CallbacksHandlerHelper {
    private final Map<String, InlineKeyboardCallbackHandler<? extends Callback>> handlersByType;
    private final Map<String, MessageCallbackHandler> handlersByStateIdentifier;

    @Autowired
    public CallbacksHandlerHelper(
            List<InlineKeyboardCallbackHandler<?>> handlers,
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

    public void handleCallback(Message message, String callbackString) throws TelegramApiException {
        ACallbackParser.CallbackTypeAndData typeAndData = ACallbackParser.parseTypeAndData(callbackString);
        InlineKeyboardCallbackHandler<? extends Callback> handler = handlersByType.get(typeAndData.type());
        if (handler == null) {
            throw new IllegalArgumentException("Cannot find handler for callback with type %s for callback %s"
                    .formatted(typeAndData.type(), callbackString)
            );
        }
        handler.handleCallback(message, typeAndData.data());
    }

    public void handleReplyKeyboardMessage(Message message, String text, IState state) throws TelegramApiException {
        MessageCallbackHandler handler = handlersByStateIdentifier.get(state.getIdentifier());
        if (handler == null) {
            throw new IllegalArgumentException("Cannot find handler for state " + state);
        }
        handler.handleCallback(message, new SomeMessage(text));
    }
}
