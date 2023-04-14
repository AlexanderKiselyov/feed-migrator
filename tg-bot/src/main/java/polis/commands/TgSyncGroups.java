package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepositoryImpl;
import polis.data.repositories.CurrentChannelRepository;
import polis.ok.OKDataCheck;
import polis.util.State;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS = """
            Список синхронизированных групп.""";
    private static final String TG_SYNC_GROUPS_INLINE = """
            Для выбора определенной группы нажмите на нужную группу.
            Для астройки автопостинга из группы нажмите 'Автопостинг' справа от группы.
            Для удаления группы нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизированных групп пуст.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и добавьте хотя бы одну группу.""";
    private final CurrentChannelRepository currentChannelRepository;
    private final AccountsRepository accountsRepository;
    private final ChannelGroupsRepositoryImpl channelGroupsRepository;
    private final OKDataCheck okDataCheck;
    private final Logger logger = LoggerFactory.getLogger(TgSyncGroups.class);

    public TgSyncGroups(String commandIdentifier, String description, CurrentChannelRepository currentChannelRepository,
                        AccountsRepository accountsRepository,
                        ChannelGroupsRepositoryImpl channelGroupsRepository,
                        OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentChannelRepository = currentChannelRepository;
        this.accountsRepository = accountsRepository;
        this.channelGroupsRepository = channelGroupsRepository;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        List<Account> accounts = accountsRepository.getAccountsForUser(chat.getId());
        if (currentChannel != null && accounts != null) {
            List<ChannelGroup> channelGroups =
                    channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId());
            if (channelGroups != null) {
                sendAnswer(absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        TG_SYNC_GROUPS,
                        rowsCount,
                        commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        TG_SYNC_GROUPS_INLINE,
                        channelGroups.size(),
                        commandsForKeyboard,
                        getTgChannelGroupsArray(channelGroups, accounts));
            } else {
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                        1,
                        List.of(State.TgChannelDescription.getDescription()),
                        null,
                        GO_BACK_BUTTON_TEXT);
            }
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                    1,
                    List.of(State.TgChannelDescription.getDescription()),
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getTgChannelGroupsArray(List<ChannelGroup> groups, List<Account> socialMediaAccounts) {
        String[] buttons = new String[groups.size() * 6];
        for (int i = 0; i < groups.size(); i++) {
            int tmpIndex = i * 6;
            String groupName = null;
            switch (groups.get(i).getSocialMedia()) {
                case OK -> {
                    for (Account socialMediaAccount : socialMediaAccounts) {
                        if (Objects.equals(socialMediaAccount.getAccountId(), groups.get(i).getAccountId())) {
                            groupName = okDataCheck.getOKGroupName(groups.get(i).getGroupId(),
                                    socialMediaAccount.getAccessToken());
                            break;
                        }
                    }
                }
                default -> logger.error(String.format("Social media not found: %s", groups.get(i).getSocialMedia()));
            }
            if (groupName != null) {
                buttons[tmpIndex] = String.format("%s (%s)", groupName,
                        groups.get(i).getSocialMedia().getName());
                buttons[tmpIndex + 1] = String.format("group %s %d", groups.get(i).getGroupId(), 0);
                buttons[tmpIndex + 2] = "\uD83D\uDD04 Автопостинг";
                buttons[tmpIndex + 3] = String.format("group %s %d", groups.get(i).getGroupId(), 2);
                buttons[tmpIndex + 4] = "\uD83D\uDDD1 Удалить";
                buttons[tmpIndex + 5] = String.format("group %s %d", groups.get(i).getGroupId(), 1);
            }
        }

        return buttons;
    }
}
