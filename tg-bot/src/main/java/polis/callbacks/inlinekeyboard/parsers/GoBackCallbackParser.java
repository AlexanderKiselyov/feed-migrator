package polis.callbacks.inlinekeyboard.parsers;

import org.springframework.stereotype.Component;
import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.objects.GoBackCallback;

import java.util.List;

@Component
public class GoBackCallbackParser extends ACallbackParser<GoBackCallback> {

    @Override
    protected String toData(GoBackCallback callback) {
        return "";
    }

    @Override
    protected GoBackCallback fromText(List<String> data) {
        return new GoBackCallback();
    }

}
