package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AccountCallback;
import polis.keyboards.callbacks.objects.GroupCallback;

import java.util.List;

@Component
public class GroupCBParser implements CallbackParser<GroupCallback> {

    @Override
    public String toText(GroupCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.groupId),
                callback.isClickForDeletion ? "1" : "0",
                callback.socialMedia
        );
    }

    @Override
    public GroupCallback fromText(List<String> data) {
        long groupId = Long.parseLong(data.get(0));
        boolean clickForDeletion = Util.isClickForDeletion(data.get(1));
        String socialMedia = data.get(2);
        return new GroupCallback(groupId, clickForDeletion, socialMedia);
    }

    @Override
    public int dataFieldsCount() {
        return 3;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GROUP_CHOSEN;
    }
}
