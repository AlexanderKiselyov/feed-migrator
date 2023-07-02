package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.YesNoCallback;

import java.util.List;

@Component
public class YesNoCallbackParser extends ACallbackParser<YesNoCallback> {

    public YesNoCallbackParser() {
        super(1);
    }

    @Override
    protected String toData(YesNoCallback callback) {
        return Util.booleanFlag(callback.yes);
    }

    @Override
    protected YesNoCallback fromText(List<String> data) {
        return new YesNoCallback(Util.booleanFlag(data.get(0)));
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.YES_NO_ANSWER;
    }
}
