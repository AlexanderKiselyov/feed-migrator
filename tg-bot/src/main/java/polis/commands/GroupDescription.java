package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION = """
            Выбрана группа <b>%s</b> из социальной сети %s.
            Вы можете выбрать команду /autoposting для настройки автопостинга.""";
    private static final String GROUP_DESCRIPTION_EXTENDED = """
            Выбрана группа <b>%s</b> из социальной сети %s.
            Вы можете выбрать команду /autoposting для настройки автопостинга.
            Настроить уведомления об автоматически опубликованных постах можно с помощью команды  /notifications.""";
    private static final String NO_VALID_GROUP = """
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

    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDescription.class);

    public GroupDescription() {
        super(State.GroupDescription.getIdentifier(), State.GroupDescription.getDescription());
        this.commandsForKeyboard.add(State.Autoposting.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String groupName = null;
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());

        if (currentGroup != null && currentAccount != null) {
            switch (currentGroup.getSocialMedia()) {
                case OK -> groupName = okDataCheck.getOKGroupName(currentGroup.getGroupId(),
                        currentAccountRepository.getCurrentAccount(chat.getId()).getAccessToken());
                case VK -> groupName = vkDataCheck.getVkGroupName(
                        new VkAuthorizator.TokenWithId(
                                currentAccount.getAccessToken(),
                                (int) currentAccount.getAccountId()
                        ),
                        currentGroup.getGroupId()
                );
                default -> LOGGER.error(String.format("Social media not found: %s",
                        currentGroup.getSocialMedia()));
            }

            if (Objects.equals(groupName, null)) {
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        GROUP_NAME_NOT_FOUND,
                        rowsCount,
                        commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
                LOGGER.error(String.format("Error detecting groupName of group: %s", currentGroup.getSocialMedia()));
                return;
            }
            long channelId = currentChannelRepository.getCurrentChannel(chat.getId()).getChannelId();
            boolean isAutopostingEnable = userChannelsRepository.isSetAutoposting(chat.getId(), channelId);
            String msgToSend = isAutopostingEnable ? GROUP_DESCRIPTION_EXTENDED : GROUP_DESCRIPTION;
            if (isAutopostingEnable && !commandsForKeyboard.contains(State.Notifications.getDescription())) {
                commandsForKeyboard.add(State.Notifications.getDescription());
                rowsCount++;
            } else if (!isAutopostingEnable && commandsForKeyboard.contains(State.Notifications.getDescription())) {
                commandsForKeyboard.remove(State.Notifications.getDescription());
                rowsCount--;
            }

            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(msgToSend, groupName,
                            currentGroup.getSocialMedia().getName()),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_VALID_GROUP, State.TgChannelDescription.getIdentifier()),
                    super.rowsCount,
                    super.commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
