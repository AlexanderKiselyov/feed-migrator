package polis.util;

import java.util.Map;
import java.util.Objects;

public enum State implements IState {
    Start("start", "\uD83D\uDC4B Старт"),
    AddTgChannel("add_tg_channel", "➕ Добавление Телеграм-канала"),
    MainMenu("main_menu", "\uD83E\uDDED Главное меню"),
    TgChannelDescription("tg_channel_description", "\uD83D\uDCC3 Информация по Телеграм-каналу"),
    TgChannelsList("tg_channels_list", "✅\uD83D\uDCC4 Список добавленных Телеграм-каналов"),
    TgSyncGroups("tg_sync_groups", "\uD83C\uDFAF\uD83D\uDCC4"
            + " Список синхронизованных с Телеграм-каналов групп"),
    GroupDescription("group_description", "\uD83D\uDCD1 Описание группы"),
    AddGroup("add_group", "➕ Добавление новой группы"),
    AddOkAccount("add_ok_account", "\uD83C\uDF10 Добавление аккаунта Одноклассников"),
    OkAccountDescription("ok_account_description", "\uD83D\uDCD1 Информация по аккаунту "
            + "Одноклассников"),
    AccountsList("accounts_list", "\uD83D\uDCC4 Список добавленных аккаунтов"),
    AddOkGroup("add_ok_group_and_sync", "➕ Добавление группы Одноклассников"),
    SyncOkGroupDescription("ok_group_description",
            "\uD83D\uDCD1 Описание синрхронизованной с Телеграм-каналом группы Одноклассников"),
    SyncOkTg("sync_ok_tg", "\uD83D\uDD04 Синхронизация группы Одноклассников с Телеграм-каналом"),
    Autoposting("autoposting", "\uD83D\uDD04 Настройка функции автопостинга"),
    Notifications("notifications", "\uD83D\uDD14 Настройка уведомлений о публикации"),
    AddVkAccount("add_vk_account", "\uD83C\uDF10 Добавление аккаунта ВКонтакте"),
    VkAccountDescription("vk_account_description", "\uD83D\uDCD1 Информация по аккаунту ВКонтакте"),
    AddVkGroup("add_vk_group", "➕ Добавление группы ВКонтакте"),
    SyncVkTg("sync_vk_tg", "\uD83D\uDD04 Синхронизация группы ВКонтакте с Телеграм-каналом");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> prevStates = Map.ofEntries(
            Map.entry(Start, Start),
            Map.entry(Substate.AddOkAccount_AuthCode, AddGroup),
            Map.entry(Substate.AddOkGroup_AddGroup, AddOkGroup),
            Map.entry(SyncOkTg, OkAccountDescription),
            Map.entry(TgSyncGroups, TgChannelDescription),
            Map.entry(GroupDescription, TgChannelDescription),
            Map.entry(AddOkAccount, AddGroup),
            Map.entry(AddGroup, TgChannelDescription),
            Map.entry(AccountsList, AddGroup),
            Map.entry(OkAccountDescription, AddGroup),
            Map.entry(AddOkGroup, OkAccountDescription),
            Map.entry(SyncOkGroupDescription, OkAccountDescription),
            Map.entry(Autoposting, GroupDescription),
            Map.entry(AddTgChannel, MainMenu),
            Map.entry(Notifications, GroupDescription),
            Map.entry(AddVkAccount, AddGroup),
            Map.entry(Substate.AddVkAccount_AuthCode, AddGroup),
            Map.entry(VkAccountDescription, AddGroup),
            Map.entry(AddVkGroup, VkAccountDescription),
            Map.entry(Substate.AddVkGroup_AddGroup, AddVkGroup)
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
