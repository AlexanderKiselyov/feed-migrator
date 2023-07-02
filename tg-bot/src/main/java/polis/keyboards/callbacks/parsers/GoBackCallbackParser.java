package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GoBackCallback;

import java.util.List;

@Component
public class GoBackCallbackParser extends ACallbackParser<GoBackCallback> {

    public GoBackCallbackParser() {
        super(0);
    }

    @Override
    protected String toData(GoBackCallback callback) {
        return "";
    }

    @Override
    protected GoBackCallback fromText(List<String> data) {
        return new GoBackCallback();
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GO_BACK;
    }
}
