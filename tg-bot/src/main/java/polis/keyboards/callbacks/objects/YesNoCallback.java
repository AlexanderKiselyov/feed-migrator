package polis.keyboards.callbacks.objects;

public final class YesNoCallback extends Callback {
    public static final YesNoCallback YES_CALLBACK = new YesNoCallback(true);
    public static final YesNoCallback NO_CALLBACK = new YesNoCallback(false);

    public final boolean yes;

    public YesNoCallback(boolean yes) {
        this.yes = yes;
    }

    public boolean yes() {
        return yes;
    }

    public boolean no() {
        return !yes;
    }
}
