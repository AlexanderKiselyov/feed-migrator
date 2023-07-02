package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.TgChannelCallback;

import java.util.List;

@Component
public class TgChannelCallbackParser implements CallbackParser<TgChannelCallback> {

    @Override
    public String toText(TgChannelCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.channelId),
                callback.isClickedForDeletion ? "1" : "0"
        );
    }

    @Override
    public TgChannelCallback fromText(List<String> data) {
        long channelId = Long.parseLong(data.get(0));
        boolean isClickForDeletion = Util.booleanFlag(data.get(1));
        return new TgChannelCallback(channelId, isClickForDeletion);
    }

    @Override
    public int dataFieldsCount() {
        return 2;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.TG_CHANNEL_CHOSEN;
    }
}
