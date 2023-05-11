package polis.commands;

import org.springframework.stereotype.Component;
import polis.util.State;

@Component
public class SyncOkTg extends SyncGroupWithChannel {

    public SyncOkTg() {
        super(State.SyncOkTg.getIdentifier(), State.SyncOkTg.getDescription());
    }
}
