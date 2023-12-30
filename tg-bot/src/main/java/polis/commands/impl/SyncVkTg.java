package polis.commands.impl;

import org.springframework.stereotype.Component;
import polis.util.IState;
import polis.util.State;

@Component
public class SyncVkTg extends SyncGroupWithChannel {
    @Override
    public IState state() {
        return State.SyncVkTg;
    }
}
