package polis.util;

import java.util.Map;
import java.util.Objects;

public enum State implements IState {
    Start("start", "Старт"),
    AddTgChannel("add_tg_channel", "Добавление Телеграм-канала"),
    MainMenu("main_menu", "Главное меню"),
    TgChannelDescription("tg_channel_description", "Информация по Телеграм-каналу"),
    TgChannelsList("tg_channels_list", "Список добавленных Телеграм-каналов"),
    TgSyncGroups("tg_sync_groups", "Список синхронизованных с Телеграм-каналов групп"),
    GroupDescription("group_description", "Описание группы"),
    OkAuth("okauth", "Авторизация в Одноклассниках"),
    Sync("sync", "Синхронизация социальных сетей с ботом");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> prevStates = Map.of(
            Start, Start,
            OkAuth, Start,
            Substate.OkAuth_AuthCode, OkAuth,
            Substate.OkAuth_GroupSync, OkAuth,
            Sync, Start,
            Substate.Sync_TelegramChannel, Sync
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

    public static State findStateByDescription(String text) {
        for (State state : State.values()) {
            if (Objects.equals(state.getDescription(), text)) {
                return state;
            }
        }
        return null;
    }

    public static IState getPrevState(IState currentState) {
        return prevStates.get(currentState);
    }
}
