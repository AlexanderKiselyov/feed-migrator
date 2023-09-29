package polis.callbacks.inlinekeyboard.parsers;

import org.springframework.stereotype.Component;
import polis.callbacks.inlinekeyboard.CallbackType;
import polis.callbacks.inlinekeyboard.objects.TgChannelCallback;

import java.util.List;

@Component
public class TgChannelCallbackParser extends ACallbackParser<TgChannelCallback> {

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

}
