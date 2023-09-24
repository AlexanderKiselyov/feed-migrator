package polis.keyboards.callbacks.handlers;

import polis.keyboards.callbacks.CallbackHandler;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.ReplyKeyboardCallback;
import polis.util.IState;

/**
 * Данные колбеки отличаются своей контекстно-зависимостью.
 * Их задача обработать текстовую информацию, но чтобы правильно интерпретировать эту текстовую информацию
 * необходимо знать текущий стейт. Это даёт понимание, на каком этапе общения с пользователем находится бот
 */
public interface ReplyKeyboardCallbackHandler extends CallbackHandler<ReplyKeyboardCallback> {
    /**
     * @return state related to this callback
     */
    IState state();

    @Override
    default CallbackType callbackType() {
        return CallbackType.CONTEXTUALIZED_MESSAGE;
    }
}
