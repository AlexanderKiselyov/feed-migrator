package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.TgChannelCallback;

import java.util.List;

@Component
public class TgChannelCallbackParser extends ACallbackParser<TgChannelCallback> {

    public TgChannelCallbackParser() {
        super(2);
    }

    @Override
    protected String toData(TgChannelCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.channelId),
                Util.booleanFlag(callback.isClickedForDeletion)
        );
    }

    @Override
    protected TgChannelCallback fromText(List<String> data) {
        return new TgChannelCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1))
        );
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.TG_CHANNEL_CHOSEN;
    }
}
