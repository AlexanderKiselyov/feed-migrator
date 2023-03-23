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
    AddGroup("add_group", "Добавление новой группы"),
    AddOkAccount("add_ok_account", "Добавление аккаунта Одноклассников"),
    OkAccountDescription("ok_account_description", "Информация по аккаунту Одноклассников"),
    AccountsList("accounts_list", "Список добавленных аккаунтов"),
    OkAccountGroups("ok_account_groups", "Список групп аккаунта Одноклассников"),
    AddOkGroup("add_ok_group", "Добавление группы Однокласников"),
    OkGroupDescription("ok_group_description", "Описание группы Одноклассников"),
    SyncOkTg("sync_ok_tg", "Синхронизация группы Одноклассников с Телеграм-каналом"),
    SyncOkTgDescription("sync_ok_tg_description", "Описание синхронизации Телеграм-канала с группой");
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
            Substate.AddOkAccount_AuthCode, AddOkAccount,
            SyncOkTg, Start,
            Substate.Sync_TelegramChannel, SyncOkTg
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
