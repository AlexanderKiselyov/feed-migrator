package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GoBackCallback;

import java.util.List;

@Component
public class GoBackCallbackParser extends ACallbackParser<GoBackCallback> {

    protected GoBackCallbackParser() {
        super(CallbackType.GO_BACK, 0);
    }

    @Override
    public String toText2(GoBackCallback callback) {
        return "";
    }

    @Override
    public GoBackCallback fromText2(List<String> data) {
        return new GoBackCallback();
    }

}
