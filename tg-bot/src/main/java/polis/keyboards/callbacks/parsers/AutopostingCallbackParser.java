package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AutopostingCallback;

import java.util.List;

@Component
public class AutopostingCallbackParser extends ACallbackParser<AutopostingCallback> {

    public AutopostingCallbackParser() {
        super(3);
    }

    @Override
    protected String toData(AutopostingCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.channelId),
                String.valueOf(callback.chatId),
                Util.booleanFlag(callback.enable)
        );
    }

    @Override
    protected AutopostingCallback fromText(List<String> data) {
        return new AutopostingCallback(
                Long.parseLong(data.get(0)),
                Long.parseLong(data.get(1)),
                Util.booleanFlag(data.get(2))
        );
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.AUTOPOSTING;
    }
}
