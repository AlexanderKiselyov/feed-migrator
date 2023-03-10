package polis.state;

import java.util.Objects;

public enum State {
    Start("start", "Старт"),
    OkAuth("okauth", "Авторизация в Одноклассниках");

    private final String identifier;
    private final String description;

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
}
