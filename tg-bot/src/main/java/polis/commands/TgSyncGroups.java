package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.util.State;

import java.util.Collections;
import java.util.List;

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
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    private static final String trashEmoji = "\uD83D\uDDD1";

    public TgSyncGroups() {
        super(State.TgSyncGroups.getIdentifier(), State.TgChannelsList.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null) {
            List<ChannelGroup> channelGroups =
                    channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId());

            if (channelGroups != null && !channelGroups.isEmpty()) {
                sendAnswer(absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        TG_SYNC_GROUPS,
                        ROWS_COUNT,
                        Collections.emptyList(),
                        null,
                        GO_BACK_BUTTON_TEXT);
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        TG_SYNC_GROUPS_INLINE,
                        channelGroups.size(),
                        Collections.emptyList(),
                        getButtonsForTgChannelGroups(channelGroups));
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
                null);
    }

    private String[] getButtonsForTgChannelGroups(List<ChannelGroup> groups) {
        String[] buttons = new String[groups.size() * 4];
        for (int i = 0; i < groups.size(); i++) {
            int tmpIndex = i * 4;

            ChannelGroup group = groups.get(i);
            buttons[tmpIndex] = String.format("%s (%s)", group.getGroupName(), group.getSocialMedia().getName());
            buttons[tmpIndex + 1] = String.format("group %s %s %d", group.getGroupId(),
                    group.getSocialMedia().getName(), 0);
            buttons[tmpIndex + 2] = trashEmoji + " Удалить";
            buttons[tmpIndex + 3] = String.format("group %s %s %d", group.getGroupId(),
                    group.getSocialMedia().getName(), 1);
        }

        return buttons;
    }
}
