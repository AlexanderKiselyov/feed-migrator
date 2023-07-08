package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.TgChannelCallback;

import java.util.List;

@Component
public class TgChannelCallbackParser extends ACallbackParser<TgChannelCallback> {

    protected TgChannelCallbackParser() {
        super(CallbackType.TG_CHANNEL_CHOSEN, 2);
    }

    @Override
    protected String toText2(TgChannelCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.channelId),
                Util.booleanFlag(callback.isClickedForDeletion)
        );
    }

    @Override
    protected TgChannelCallback fromText2(List<String> data) {
        return new TgChannelCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1))
        );
    }

}
