package polis.util;

import java.util.Map;

public enum Substate implements IState {
    AddOkAccount_AuthCode(State.AddOkAccount.getIdentifier(), "Получение кода авторизации Одноклассников"),
    AddOkGroup_AddGroup(State.AddOkGroup.getIdentifier(), "Добавление новой группы Одноклассников"),
    Sync_TelegramChannel(State.SyncOkTg.getIdentifier(), "Синхронизация бота с Телеграм-каналом");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> NEXT_SUBSTATE = Map.of(
            State.AddOkAccount, AddOkAccount_AuthCode,
            State.AddOkGroup, AddOkGroup_AddGroup,
            State.SyncOkTg, Sync_TelegramChannel
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
