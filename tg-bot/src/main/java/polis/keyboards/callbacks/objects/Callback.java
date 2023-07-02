package polis.keyboards.callbacks.objects;

import java.io.Serializable;

public abstract sealed class Callback implements Serializable
        permits AccountCallback, GroupCallback, TgChannelCallback, YesNoCallback {
}
