package polis.state;

import java.util.Objects;

public enum State {
    Start("start"),
    OkAuth("okauth");

    private final String identifier;

    State(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static State findState(String text) {
        for (State state : State.values()) {
            if (Objects.equals(state.getIdentifier(), text)) {
                return state;
            }
        }
        return null;
    }
}
