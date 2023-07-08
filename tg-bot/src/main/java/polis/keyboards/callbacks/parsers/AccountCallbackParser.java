package polis.keyboards.callbacks.parsers;

import org.springframework.stereotype.Component;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.AccountCallback;

import java.util.List;

@Component
public class AccountCallbackParser extends ACallbackParser<AccountCallback> {

    protected AccountCallbackParser() {
        super(CallbackType.ACCOUNT_CHOSEN, 3);
    }

    @Override
    public String toText2(AccountCallback callback) {
        return String.join(FIELDS_SEPARATOR,
                String.valueOf(callback.accountId),
                Util.booleanFlag(callback.isClickedForDeletion),
                callback.socialMedia
        );
    }

    @Override
    public AccountCallback fromText2(List<String> data) {
        return new AccountCallback(
                Long.parseLong(data.get(0)),
                Util.booleanFlag(data.get(1)),
                data.get(2)
        );
    }

}
