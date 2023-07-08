package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.GroupCallback;

import java.util.List;

@Component
public class GroupCallbackParser extends ACallbackParser<GroupCallback> {

    protected GroupCallbackParser() {
        super(CallbackType.GROUP_CHOSEN, 3);
    }

    @Override
    public String toText2(GroupCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.groupId),
                Util.booleanFlag(callback.isClickForDeletion),
                callback.socialMedia
        );
    }

    @Override
    public GroupCallback fromText2(List<String> data) {
        return new GroupCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1)),
                data.get(2)
        );
    }

}
