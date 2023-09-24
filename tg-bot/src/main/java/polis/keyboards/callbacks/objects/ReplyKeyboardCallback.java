package polis.keyboards.callbacks.objects;

/**
 * Message sent using reply keyboard
 */
public final class ReplyKeyboardCallback extends Callback {
    public final String text;

    public ReplyKeyboardCallback(String text) {
        this.text = text;
    }
}
