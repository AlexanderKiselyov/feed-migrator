package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.YesNoCallback;

import java.util.List;

@Component
public class YesNoCallbackParser extends ACallbackParser<YesNoCallback> {

    protected YesNoCallbackParser() {
        super(CallbackType.YES_NO_ANSWER, 1);
    }

    @Override
    public String toText2(YesNoCallback callback) {
        return Util.booleanFlag(callback.yesOrNo);
    }

    @Override
    public YesNoCallback fromText2(List<String> data) {
        return new YesNoCallback(Util.booleanFlag(data.get(0)));
    }

}
