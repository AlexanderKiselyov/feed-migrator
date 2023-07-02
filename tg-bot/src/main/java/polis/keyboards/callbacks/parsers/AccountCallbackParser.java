package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AccountCallback;

import java.util.List;

@Component
public class AccountCallbackParser extends ACallbackParser<AccountCallback> {

    public AccountCallbackParser() {
        super(3);
    }

    @Override
    protected String toData(AccountCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.accountId),
                Util.booleanFlag(callback.isClickedForDeletion),
                callback.socialMedia
        );
    }

    @Override
    protected AccountCallback fromText(List<String> data) {
        return new AccountCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1)),
                data.get(2)
        );
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.ACCOUNT_CHOSEN;
    }
}
