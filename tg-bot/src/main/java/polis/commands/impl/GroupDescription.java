package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.UserChannelsRepository;
import polis.util.IState;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION_MSG = """
            Выбрана группа <b>%s</b> из социальной сети %s.
            Вы можете выбрать команду /%s для настройки автопостинга.""";
    private static final String GROUP_DESCRIPTION_EXTENDED_MSG = """
            Выбрана группа <b>%s</b> из социальной сети %s.
            Вы можете выбрать команду /%s для настройки автопостинга.
            Настроить уведомления об автоматически опубликованных постах можно с помощью команды /%s.""";
    private static final String NO_VALID_GROUP_MSG = """
            Ошибка выбора группы.
            Пожалуйста, вернитесь в описание Телеграмм-канала (/%s) и выберите нужную группу.""";

    private int rowsCount = 1;
    private static final List<String> commandsForKeyboard = new ArrayList<>();

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    static {
        commandsForKeyboard.add(State.Autoposting.getDescription());
    }

    @Override
    public IState state() {
        return State.GroupDescription;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        ChannelGroup currentGroup = context.currentGroup();
        Account currentAccount = context.currentAccount();
        CurrentChannel currentChannel = context.currentChannel();

        if (currentGroup != null && currentAccount != null) {
            String groupName = currentGroup.getGroupName();
            long channelId = currentChannel.getChannelId();
            boolean isAutopostingEnable = userChannelsRepository.isSetAutoposting(chat.getId(), channelId);
            String msgToSend = isAutopostingEnable
                    ? String.format(GROUP_DESCRIPTION_EXTENDED_MSG, groupName, currentGroup.getSocialMedia().getName(),
                            State.Autoposting.getIdentifier(), State.Notifications.getIdentifier())
                    : String.format(GROUP_DESCRIPTION_MSG, groupName, currentGroup.getSocialMedia().getName(),
                            State.Autoposting.getIdentifier());
            if (isAutopostingEnable && !commandsForKeyboard.contains(State.Notifications.getDescription())) {
                commandsForKeyboard.add(State.Notifications.getDescription());
                rowsCount++;
            } else if (!isAutopostingEnable && commandsForKeyboard.contains(State.Notifications.getDescription())) {
                commandsForKeyboard.remove(State.Notifications.getDescription());
                rowsCount--;
            }

            sendAnswerWithReplyKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    msgToSend,
                    rowsCount,
                    commandsForKeyboard,
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithOnlyBackButton(
                absSender,
                chat.getId(),
                String.format(NO_VALID_GROUP_MSG, State.TgChannelDescription.getIdentifier()),
                loggingInfo(user.getUserName()));
    }
}
