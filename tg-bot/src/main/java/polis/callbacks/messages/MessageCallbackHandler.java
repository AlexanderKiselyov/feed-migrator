package polis.callbacks.messages;

import polis.callbacks.CallbackHandler;
import polis.util.IState;

/**
 * Данные колбеки отличаются своей контекстно-зависимостью.
 * Их задача обработать текстовую информацию, но чтобы правильно интерпретировать эту текстовую информацию
 * необходимо знать текущий стейт. Это даёт понимание, на каком этапе общения с пользователем находится бот
 */
public interface MessageCallbackHandler extends CallbackHandler<SomeMessage> {
    /**
     * @return state related to this callback
     */
    IState state();
}
