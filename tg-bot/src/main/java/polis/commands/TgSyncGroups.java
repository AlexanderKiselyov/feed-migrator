package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS = """
            Список синхронизированных групп.""";
    private static final String TG_SYNC_GROUPS_INLINE = """
            Для выбора определенной группы нажмите на нужную группу.
            Для удаления группы нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизированных групп пуст.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и добавьте хотя бы одну группу.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    private static final Logger LOGGER = LoggerFactory.getLogger(TgSyncGroups.class);

    public TgSyncGroups() {
        super(State.TgSyncGroups.getIdentifier(), State.TgChannelsList.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        List<Account> accounts = accountsRepository.getAccountsForUser(chat.getId());
        if (currentChannel != null && accounts != null && !accounts.isEmpty()) {
            List<ChannelGroup> channelGroups =
                    channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId());

            if (channelGroups != null && !channelGroups.isEmpty()) {
                String groupName = "";

                for (ChannelGroup group : channelGroups) {
                    switch (group.getSocialMedia()) {
                        case OK -> {
                            for (Account socialMediaAccount : accounts) {
                                if (Objects.equals(socialMediaAccount.getAccountId(), group.getAccountId())) {
                                    groupName = okDataCheck.getOKGroupName(group.getGroupId(),
                                            socialMediaAccount.getAccessToken());
                                    break;
                                }
                            }
                        }
                        case VK -> {
                            for (Account socialMediaAccount : accounts) {
                                if (Objects.equals(socialMediaAccount.getAccountId(), group.getAccountId())) {
                                    groupName = vkDataCheck.getVkGroupName(new VkAuthorizator.TokenWithId(
                                                    socialMediaAccount.getAccessToken(),
                                                    (int) socialMediaAccount.getAccountId()
                                            ),
                                            String.valueOf(group.getGroupId())
                                    );
                                    break;
                                }
                            }
                        }
                        default -> LOGGER.error(String.format("Social media not found: %s", group.getSocialMedia()));
                    }
                }

                if (Objects.equals(groupName, "")) {
                    sendAnswer(
                            absSender,
                            chat.getId(),
                            this.getCommandIdentifier(),
                            user.getUserName(),
                            GROUP_NAME_NOT_FOUND,
                            1,
                            List.of(State.TgChannelDescription.getDescription()),
                            null,
                            GO_BACK_BUTTON_TEXT);
                    return;
                }

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
                        getTgChannelGroupsArray(channelGroups, groupName));
                return;
            }
        }
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

    private String[] getTgChannelGroupsArray(List<ChannelGroup> groups, String groupName) {
        String[] buttons = new String[groups.size() * 4];
        for (int i = 0; i < groups.size(); i++) {
            int tmpIndex = i * 4;

            buttons[tmpIndex] = String.format("%s (%s)", groupName,
                    groups.get(i).getSocialMedia().getName());
            buttons[tmpIndex + 1] = String.format("group %s %d", groups.get(i).getGroupId(), 0);
            buttons[tmpIndex + 2] = "\uD83D\uDDD1 Удалить";
            buttons[tmpIndex + 3] = String.format("group %s %d", groups.get(i).getGroupId(), 1);
        }

        return buttons;
    }
}
