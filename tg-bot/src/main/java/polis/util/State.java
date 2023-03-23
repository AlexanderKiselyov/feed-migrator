package polis.util;

import java.util.Map;
import java.util.Objects;

public enum State implements IState {
    Start("start", "\uD83D\uDC4B Старт"),
    AddTgChannel("add_tg_channel", "➕ Добавление Телеграм-канала"),
    MainMenu("main_menu", "\uD83E\uDDED Главное меню"),
    TgChannelDescription("tg_channel_description", "\uD83D\uDCC3 Информация по Телеграм-каналу"),
    TgChannelsList("tg_channels_list", "✅\uD83D\uDCC4 Список добавленных Телеграм-каналов"),
    TgSyncGroups("tg_sync_groups", "\uD83C\uDFAF\uD83D\uDCC4" +
            " Список синхронизованных с Телеграм-каналов групп"),
    GroupDescription("group_description", "\uD83D\uDCD1 Описание группы"),
    AddGroup("add_group", "➕ Добавление новой группы"),
    AddOkAccount("add_ok_account", "\uD83C\uDF10 Добавление аккаунта Одноклассников"),
    OkAccountDescription("ok_account_description", "\uD83D\uDCD1 Информация по аккаунту " +
            "Одноклассников"),
    AccountsList("accounts_list", "\uD83D\uDCC4 Список добавленных аккаунтов"),
    OkAccountGroups("ok_account_groups", "\uD83D\uDCC4 Список групп аккаунта Одноклассников"),
    AddOkGroup("add_ok_group", "➕ Добавление группы Однокласников"),
    OkGroupDescription("ok_group_description", "\uD83D\uDCD1 Описание группы Одноклассников"),
    SyncOkTg("sync_ok_tg", "\uD83D\uDD04 Синхронизация группы Одноклассников с Телеграм-каналом"),
    SyncOkTgDescription("sync_ok_tg_description", "\uD83D\uDD04 Описание синхронизации " +
            "Телеграм-канала с группой");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> prevStates = Map.ofEntries(
            Map.entry(Start, Start),
            Map.entry(Substate.AddOkAccount_AuthCode, AddOkAccount),
            Map.entry(Substate.Sync_TelegramChannel, SyncOkTg),
            Map.entry(TgSyncGroups, TgChannelDescription),
            Map.entry(GroupDescription, TgChannelDescription),
            Map.entry(AddOkAccount, AddGroup),
            Map.entry(AddGroup, TgChannelDescription),
            Map.entry(AccountsList, AddGroup),
            Map.entry(OkAccountDescription, AddGroup),
            Map.entry(AddOkGroup, OkAccountGroups),
            Map.entry(OkAccountGroups, OkAccountDescription),
            Map.entry(OkGroupDescription, OkAccountDescription),
            Map.entry(SyncOkTg, OkGroupDescription),
            Map.entry(SyncOkTgDescription, OkGroupDescription)
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
