package polis.keyboards.callbacks.parsers;

import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.YesNoCallback;

import java.util.List;

public class YesNoCBParser implements CallbackParser<YesNoCallback> {

    @Override
    public String toText(YesNoCallback callback) {
        return callback.yesOrNo ? "1" : "0";
    }

    @Override
    public YesNoCallback fromText(List<String> data) {
        boolean yesOrNo = Util.booleanFlag(data.get(0));
        return new YesNoCallback(yesOrNo);
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
