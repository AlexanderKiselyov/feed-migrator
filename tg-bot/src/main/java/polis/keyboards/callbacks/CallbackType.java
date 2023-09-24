package polis.keyboards.callbacks;

public enum CallbackType {
    TG_CHANNEL_CHOSEN("tg_channel"),
    GROUP_CHOSEN("group"),
    ACCOUNT_CHOSEN("account"),
    YES_NO_ANSWER("yesNo"),
    AUTOPOSTING("autoposting"),
    NOTIFICATIONS("notifications"),
    GO_BACK("go-back"),
    EMPTY("NO_CALLBACK_TEXT"),
    REPLY_KEYBOARD_MESSAGE(null);

    public final String stringKey;

    CallbackType(String stringKey) {
        this.stringKey = stringKey;
    }
}
