package polis.callbacks.typed.parsers;

import org.springframework.stereotype.Component;
import polis.callbacks.typed.objects.GroupCallback;

import java.util.List;

@Component
public class GroupCallbackParser extends ACallbackParser<GroupCallback> {

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

}
