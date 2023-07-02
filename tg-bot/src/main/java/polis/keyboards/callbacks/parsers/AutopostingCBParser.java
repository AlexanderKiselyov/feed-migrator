package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AutopostingCallback;

import java.util.List;

@Component
public class AutopostingCBParser implements CallbackParser<AutopostingCallback> {
    @Override
    public String toText(AutopostingCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.channelId),
                String.valueOf(callback.chatId),
                Util.booleanFlag(callback.isEnabled)
        );
    }

    @Override
    public AutopostingCallback fromText(List<String> data) {
        return null;
    }

    @Override
    public int dataFieldsCount() {
        return 3;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.AUTOPOSTING;
    }
}
