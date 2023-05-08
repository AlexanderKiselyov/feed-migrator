package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.util.Emojis;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS_MSG = """
            Список синхронизированных групп.""";
    private static final String TG_SYNC_GROUPS_INLINE_MSG = """
            Список синхронизированных групп.
            Для выбора определенной группы нажмите на нужную группу.
            Для удаления группы нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизированных групп пуст.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и добавьте хотя бы одну группу.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

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
                sendAnswerWithInlineKeyboardAndBackButton(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        TG_SYNC_GROUPS_MSG,
                        TG_SYNC_GROUPS_INLINE_MSG,
                        channelGroups.size(),
                        getButtonsForTgChannelGroups(channelGroups));
                return;
            }
        }
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                1,
                List.of(State.TgChannelDescription.getDescription()));
    }

    private List<String> getButtonsForTgChannelGroups(List<ChannelGroup> groups) {
        List<String> buttons = new ArrayList<>(groups.size() * 4);
        for (ChannelGroup group : groups) {
            String socialMediaName = group.getSocialMedia().getName();
            long groupId = group.getGroupId();
            buttons.add(String.format("%s (%s)", group.getGroupName(), socialMediaName));
            buttons.add(String.format("group %s %d %s", groupId, 0, socialMediaName));
            buttons.add(Emojis.TRASH + " Удалить");
            buttons.add(String.format("group %s %d %s", groupId, 1, socialMediaName));
        }
        return buttons;
    }
}
