package polis.callbacks.typed.parsers;

import org.springframework.stereotype.Component;
import polis.callbacks.typed.objects.NotificationsCallback;

import java.util.List;

@Component
public class NotificationCallbackParser extends ACallbackParser<NotificationsCallback> {

    @Override
    protected String toData(NotificationsCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.chatId),
                Util.booleanFlag(callback.isEnabled)
        );
    }

    @Override
    protected NotificationsCallback fromText(List<String> data) {
        return new NotificationsCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1))
        );
    }

}
