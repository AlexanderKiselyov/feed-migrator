package polis.callbacks.inlinekeyboard.parsers;

import org.springframework.stereotype.Component;
import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.objects.AutopostingCallback;

import java.util.List;

@Component
public class AutopostingCallbackParser extends ACallbackParser<AutopostingCallback> {

    @Override
    protected String toData(AutopostingCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.chatId),
                String.valueOf(callback.channelId),
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
}
