package polis.callbacks.messages;

import polis.callbacks.Callback;

/**
 * Message sent using reply keyboard
 */
public final class SomeMessage implements Callback {
    public final String text;

    public SomeMessage(String text) {
        this.text = text;
    }
}
