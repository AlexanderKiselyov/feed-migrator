package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AutopostingCallback;

import java.util.List;

@Component
public class AutopostingCallbackParser extends ACallbackParser<AutopostingCallback> {

    protected AutopostingCallbackParser() {
        super(CallbackType.AUTOPOSTING, 3);
    }

    @Override
    public String toText2(AutopostingCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.channelId),
                String.valueOf(callback.chatId),
                Util.booleanFlag(callback.enableOrDisable)
        );
    }

    @Override
    public AutopostingCallback fromText2(List<String> data) {
        return new AutopostingCallback(
                Long.parseLong(data.get(0)),
                Long.parseLong(data.get(1)),
                Util.booleanFlag(data.get(2))
        );
    }

}
