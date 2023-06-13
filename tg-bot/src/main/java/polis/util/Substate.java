package polis.util;

import java.util.Map;
import java.util.Objects;

public enum Substate implements IState {
    AddOkAccount_AuthCode(State.AddOkAccount.getIdentifier(), "Получение кода авторизации Одноклассников"),
    AddOkGroup_AddGroup(State.AddOkGroup.getIdentifier(), "Добавление новой группы Одноклассников"),
    AddVkAccount_AccessToken(State.AddVkAccount.getIdentifier(), "Получение токена доступа ВКонтакте"),
    AddVkGroup_AddGroup(State.AddVkGroup.getIdentifier(), "Добавление новой группы ВКонтакте");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> NEXT_SUBSTATE = Map.of(
            State.AddOkAccount, AddOkAccount_AuthCode,
            State.AddOkGroup, AddOkGroup_AddGroup,
            State.AddVkAccount, AddVkAccount_AccessToken,
            State.AddVkGroup, AddVkGroup_AddGroup
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

    public static Substate findSubstate(String name) {
        for (Substate substate : Substate.values()) {
            if (Objects.equals(substate.getIdentifier(), name)) {
                return substate;
            }
        }
        return null;
    }
}
