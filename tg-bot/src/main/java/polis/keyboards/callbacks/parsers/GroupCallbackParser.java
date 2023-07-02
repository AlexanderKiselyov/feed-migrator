package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GroupCallback;

import java.util.List;

@Component
public class GroupCallbackParser extends ACallbackParser<GroupCallback> {

    public GroupCallbackParser() {
        super(3);
    }

    @Override
    protected String toData(GroupCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.groupId),
                Util.booleanFlag(callback.isClickForDeletion),
                callback.socialMedia
        );
    }

    @Override
    protected GroupCallback fromText(List<String> data) {
        return new GroupCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1)),
                data.get(2)
        );
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.GROUP_CHOSEN;
    }
}
