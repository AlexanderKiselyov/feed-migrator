package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GoBackCallback;

import java.util.List;

@Component
public class GoBackCBParser implements CallbackParser<GoBackCallback> {
    @Override
    public String toText(GoBackCallback callback) {
        return "";
    }

    @Override
    public GoBackCallback fromText(List<String> data) {
        return new GoBackCallback();
    }

    @Override
    public int dataFieldsCount() {
        return 0;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GO_BACK;
    }
}
