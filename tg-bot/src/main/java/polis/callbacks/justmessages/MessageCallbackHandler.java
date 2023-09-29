package polis.callbacks.justmessages;

import org.telegram.telegrambots.meta.api.objects.Message;
import polis.callbacks.CallbackHandler;
import polis.util.IState;

/**
 * Хэндлер, умеюший обработать колбек типа SomeMessage
 * Хэндлеры таких калбеков отличаются своей контекстно-зависимостью.
 * Их задача обработать текстовую информацию, но чтобы правильно интерпретировать эту текстовую информацию
 * необходимо знать текущий стейт - понимание, на каком этапе общения с пользователем находится бот
 *
 * @see polis.callbacks.CallbacksHandlerHelper#handleMessageCallback(String, IState, Message)
 * @see IState
 */
public interface MessageCallbackHandler extends CallbackHandler<SomeMessage> {
    /**
     * @return state related to this callback
     */
    IState state();
}
