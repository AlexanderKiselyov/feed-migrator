package polis.util;

import java.util.Map;
import java.util.Objects;

public enum State implements IState {
    Start("start", Emojis.HELLO_HAND + " Старт"),
    AddTgChannel("add_tg_channel", Emojis.PLUS + " Добавление Телеграмм-канала"),
    Help("help", Emojis.HELP + " Справка по боту"),
    MainMenu("main_menu", Emojis.COMPASS + " Главное меню"),
    TgChannelDescription("tg_channel_description", Emojis.PAPER_LIST
            + " Информация по Телеграмм-каналу"),
    TgChannelsList("tg_channels_list", Emojis.CHECKBOX + Emojis.PAPER_LIST_2
            + " Список добавленных Телеграмм-каналов"),
    TgSyncGroups("tg_sync_groups", Emojis.TARGET + Emojis.PAPER_LIST_2
            + " Список синхронизованных с Телеграмм-каналов групп"),
    GroupDescription("group_description", Emojis.PAPER_LIST_3 + " Описание группы"),
    AddGroup("add_group", Emojis.PLUS + " Добавление новой группы"),
    AddOkAccount("add_ok_account", Emojis.WORLD + " Добавление аккаунта Одноклассников"),
    OkAccountDescription("ok_account_description", Emojis.PAPER_LIST_3 + " Информация по аккаунту "
            + "Одноклассников"),
    AccountsList("accounts_list", Emojis.PAPER_LIST_2 + " Список добавленных аккаунтов"),
    AddOkGroup("add_ok_group_and_sync", Emojis.PLUS + " Добавление группы Одноклассников"),
    SyncGroupDescription("group_description",
            Emojis.PAPER_LIST_3 + " Описание синрхронизованной с Телеграмм-каналом группы Одноклассников"),
    SyncOkTg("sync_ok_tg", Emojis.SYNC + " Синхронизация группы Одноклассников с Телеграмм-каналом"),
    Autoposting("autoposting", Emojis.SYNC + " Настройка функции автопостинга"),
    Notifications("notifications", Emojis.BELL + " Настройка уведомлений о публикации"),
    AddVkAccount("add_vk_account", Emojis.WORLD + " Добавление аккаунта ВКонтакте"),
    VkAccountDescription("vk_account_description", Emojis.PAPER_LIST_3
            + " Информация по аккаунту ВКонтакте"),
    AddVkGroup("add_vk_group", Emojis.PLUS + " Добавление группы ВКонтакте"),
    SyncVkTg("sync_vk_tg", Emojis.SYNC + " Синхронизация группы ВКонтакте с Телеграмм-каналом");

    private final String identifier;
    private final String description;
    private static final Map<IState, IState> prevStates = Map.ofEntries(
            Map.entry(Start, Start),
            Map.entry(AddTgChannel, MainMenu),
            Map.entry(Help, MainMenu),
            Map.entry(TgChannelsList, MainMenu),
            Map.entry(TgSyncGroups, TgChannelDescription),
            Map.entry(GroupDescription, TgChannelDescription),
            Map.entry(AddGroup, TgChannelDescription),
            Map.entry(AddOkAccount, AddGroup),
            Map.entry(OkAccountDescription, AddGroup),
            Map.entry(AccountsList, AddGroup),
            Map.entry(AddOkGroup, OkAccountDescription),
            Map.entry(SyncGroupDescription, OkAccountDescription),
            Map.entry(SyncOkTg, OkAccountDescription),
            Map.entry(Autoposting, GroupDescription),
            Map.entry(Notifications, GroupDescription),
            Map.entry(AddVkAccount, AddGroup),
            Map.entry(VkAccountDescription, AddGroup),
            Map.entry(AddVkGroup, VkAccountDescription),
            Map.entry(SyncVkTg, VkAccountDescription)
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
