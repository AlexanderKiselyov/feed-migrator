package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.NotificationsCallback;

import java.util.List;

@Component
public class NotificationCallbackParser extends ACallbackParser<NotificationsCallback> {

    protected NotificationCallbackParser() {
        super(CallbackType.NOTIFICATIONS, 2);
    }

    @Override
    protected String toText2(NotificationsCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.chatId),
                Util.booleanFlag(callback.isEnabled)
        );
    }

    @Override
    protected NotificationsCallback fromText2(List<String> data) {
        return new NotificationsCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1))
        );
    }

}
