package polis.commands;

import org.springframework.stereotype.Component;
import polis.util.State;

@Component
public class SyncVkTg extends SyncGroupWithChannel {

    public SyncVkTg() {
        super(State.SyncVkTg.getIdentifier(), State.SyncVkTg.getDescription());
    }
}
