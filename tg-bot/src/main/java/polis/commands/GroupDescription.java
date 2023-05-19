package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.UserChannelsRepository;
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
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и выберите нужную группу.""";

    private int rowsCount = 1;
    private final List<String> commandsForKeyboard = new ArrayList<>();

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    public GroupDescription() {
        super(State.GroupDescription.getIdentifier(), State.GroupDescription.getDescription());
        this.commandsForKeyboard.add(State.Autoposting.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());

        if (currentGroup != null && currentAccount != null) {
            String groupName = currentGroup.getGroupName();
            long channelId = currentChannelRepository.getCurrentChannel(chat.getId()).getChannelId();
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
