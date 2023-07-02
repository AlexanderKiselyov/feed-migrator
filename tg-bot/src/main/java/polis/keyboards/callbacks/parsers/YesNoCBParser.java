package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.YesNoCallback;

import java.util.List;

@Component
public class YesNoCBParser implements CallbackParser<YesNoCallback> {

    @Override
    public String toText(YesNoCallback callback) {
        return Util.booleanFlag(callback.yesOrNo);
    }

    @Override
    public YesNoCallback fromText(List<String> data) {
        return new YesNoCallback(Util.booleanFlag(data.get(0)));
    }

    @Override
    public int dataFieldsCount() {
        return 1;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.YES_NO_ANSWER;
    }
}
