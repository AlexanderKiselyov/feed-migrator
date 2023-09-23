package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.NotificationsCallback;

import java.util.List;

@Component
public class NotificationCallbackParser extends ACallbackParser<NotificationsCallback> {

    public NotificationCallbackParser() {
        super(2);
    }

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

    @Override
    public CallbackType callbackType() {
        return CallbackType.NOTIFICATIONS;
    }
}