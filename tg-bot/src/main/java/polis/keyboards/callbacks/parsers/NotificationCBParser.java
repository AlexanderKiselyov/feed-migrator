package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.NotificationsCallback;

import java.util.List;

@Component
public class NotificationCBParser implements CallbackParser<NotificationsCallback> {

    @Override
    public String toText(NotificationsCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.chatId),
                Util.booleanFlag(callback.isEnabled)
        );
    }

    @Override
    public NotificationsCallback fromText(List<String> data) {
        long chatId = Long.parseLong(data.get(0));
        boolean isEnabled = Util.booleanFlag(data.get(1));
        return new NotificationsCallback(chatId, isEnabled);
    }

    @Override
    public int dataFieldsCount() {
        return 2;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.NOTIFICATIONS;
    }
}