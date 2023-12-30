package polis.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.justmessages.MessageCallbackHandler;
import polis.callbacks.justmessages.SomeMessage;
import polis.util.AnswerPair;
import polis.commands.context.Context;
import polis.datacheck.VkDataCheck;
import polis.util.IState;
import polis.util.State;

import java.util.Collections;
import java.util.List;

@Component
public class AddVkAccountAccessTokenHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {

    private static final List<String> KEYBOARD_BUTTONS = List.of(State.VkAccountDescription.getDescription());

    @Autowired
    private VkDataCheck vkDataCheck;

    @Override
    public IState state() {
        return State.AddVkAccount;
    }

    @Override
    protected void handleCallback(long userChatId, Message message, SomeMessage callback, Context context) throws TelegramApiException {
        AnswerPair answerPair = vkDataCheck.getVkAccessToken(callback.text, message.getChatId());
        List<String> keyboardButtons = !answerPair.getError() ? KEYBOARD_BUTTONS : Collections.emptyList();
        sendAnswer(userChatId, getUserName(message), answerPair.getAnswer(), keyboardButtons);
    }
}
