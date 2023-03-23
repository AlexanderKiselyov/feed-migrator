package polis.util;

import java.util.Map;
import java.util.Objects;

public enum State implements IState {
    Start("start", "\uD83D\uDC4B Старт"),
    AddTgChannel("add_tg_channel", "➕ Добавление Телеграм-канала"),
    MainMenu("main_menu", "\uD83E\uDDED Главное меню"),
    TgChannelDescription("tg_channel_description", "\uD83D\uDCC3 Информация по Телеграм-каналу"),
    TgChannelsList("tg_channels_list", "✅\uD83C\uDFAF\uD83E\uDEAA Список добавленных Телеграм-каналов"),
    TgSyncGroups("tg_sync_groups", "✅\uD83C\uDFAF Список синхронизованных с Телеграм-каналов групп"),
    GroupDescription("group_description", "\uD83D\uDCD1 Описание группы"),
    OkAuth("okauth", "\uD83C\uDF10\uD83E\uDEAA Авторизация в Одноклассниках"),
    Sync("sync", "\uD83D\uDD04 Синхронизация социальных сетей с ботом");

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
