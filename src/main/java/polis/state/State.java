package polis.state;

import java.util.Map;
import java.util.Objects;

public enum State {
    Start("start", "Старт"),
    OkAuth("okauth", "Авторизация в Одноклассниках");

    private final String identifier;
    private final String description;
    private static final Map<State, State> prevStates = Map.of(
            Start, Start,
            OkAuth, Start
    );

    State(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public static State findState(String text) {
        for (State state : State.values()) {
            if (Objects.equals(state.getIdentifier(), text)) {
                return state;
            }
        }
        return null;
    }

    public static State getPrevState(State currentState) {
        return prevStates.get(currentState);
    }
}
