package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AccountCallback;

import java.util.List;

@Component
public class AccountCallbackParser implements CallbackParser<AccountCallback> {
    private static final int ACCOUNT_ID_INDEX = 0;
    private static final int IS_DELETION_REQUESTED_FLAG_INDEX = 1;
    private static final int SOCIAL_MEDIA_NAME_INDEX = 2;

    @Override
    public String toText(AccountCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.accountId),
                callback.isClickedForDeletion ? "1" : "0",
                callback.socialMedia
        );
    }

    @Override
    public AccountCallback fromText(List<String> data) {
        long accountId = Long.parseLong(data.get(ACCOUNT_ID_INDEX));
        boolean clickForDeletion = Util.isClickForDeletion(data.get(IS_DELETION_REQUESTED_FLAG_INDEX));
        String socialMedia = data.get(SOCIAL_MEDIA_NAME_INDEX);
        return new AccountCallback(accountId, clickForDeletion, socialMedia);
    }

    @Override
    public int dataFieldsCount() {
        return 3;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.ACCOUNT_CHOSEN;
    }

}
