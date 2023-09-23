package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.keyboards.callbacks.objects.GroupCallback;
import polis.keyboards.callbacks.parsers.GroupCallbackParser;
import polis.util.Emojis;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS_MSG = """
            Список синхронизированных групп.
            Для выбора определенной группы нажмите на нужную группу.
            Для удаления группы нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизированных групп пуст.
            Пожалуйста, вернитесь в описание Телеграмм-канала (/%s) и добавьте хотя бы одну группу.""";
    private static final String GROUP_INFO = "%s (%s)";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(
            State.TgChannelDescription.getDescription());

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private GroupCallbackParser groupCallbackParser;

    public TgSyncGroups(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.TgSyncGroups.getIdentifier(), State.TgChannelsList.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null) {
            List<ChannelGroup> channelGroups =
                    channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId());

            if (channelGroups != null && !channelGroups.isEmpty()) {
                sendAnswerWithInlineKeyboard(
                        absSender,
                        chat.getId(),
                        TG_SYNC_GROUPS_MSG,
                        channelGroups.size(),
                        getButtonsForTgChannelGroups(channelGroups),
                        loggingInfo(user.getUserName()));
                return;
            }
        }
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }

    private List<String> getButtonsForTgChannelGroups(List<ChannelGroup> groups) {
        List<String> buttons = new ArrayList<>(groups.size() * 4);
        for (ChannelGroup group : groups) {
            String socialMediaName = group.getSocialMedia().getName();
            long groupId = group.getGroupId();
            buttons.add(String.format(GROUP_INFO, group.getGroupName(), socialMediaName));
            buttons.add(groupCallbackParser.toText(new GroupCallback(groupId, false, socialMediaName)));
            buttons.add(Emojis.TRASH + DELETE_MESSAGE);
            buttons.add(groupCallbackParser.toText(new GroupCallback(groupId, true, socialMediaName)));
        }
        return buttons;
    }
}
