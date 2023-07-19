package polis.keyboards.callbacks.objects;

public final class YesNoCallback extends Callback {
    public static final YesNoCallback YES_CALLBACK = new YesNoCallback(true);
    public static final YesNoCallback NO_CALLBACK = new YesNoCallback(false);

    public final boolean yesOrNo;

    public YesNoCallback(boolean yesOrNo) {
        this.yesOrNo = yesOrNo;
    }
}
