package polis.util;

import java.util.Map;

public enum Substate implements IState {
    OkAuth_AuthCode(State.OkAuth.getIdentifier(), "Получение кода авторизации Одноклассников"),
    OkAuth_GroupSync(State.OkAuth.getIdentifier(), "Синхронизация бота с группой Одноклассников"),
    Sync_TelegramChannel(State.Sync.getIdentifier(), "Синхронизация бота с Телеграм-каналом");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> NEXT_SUBSTATE = Map.of(
            State.OkAuth, OkAuth_AuthCode,
            OkAuth_AuthCode, OkAuth_GroupSync,
            State.Sync, Sync_TelegramChannel
    );

    Substate(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public static IState nextSubstate(IState currentSubstate) {
        IState nextSubstate = NEXT_SUBSTATE.get(currentSubstate);
        return nextSubstate == null ? currentSubstate : nextSubstate;
    }
}
