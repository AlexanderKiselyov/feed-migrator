package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.telegram.TelegramDataCheck;
import polis.util.State;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class SyncGroupDescription extends Command {
    private static final String SYNC_OK_TG_DESCRIPTION = """
            Телеграм-канал <b>%s</b> и группа <b>%s (%s)</b> были успешно синхронизированы.
            Настроить функцию автопостинга можно по команде /%s.""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION = """
            Невозможно показать информацию по связанным Телеграм-каналу и группе.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.Autoposting.getDescription()
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncGroupDescription.class);

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private TelegramDataCheck telegramDataCheck;

    @Autowired
    private CommandUtils commandUtils;

    public SyncGroupDescription() {
        super(State.SyncGroupDescription.getIdentifier(), State.SyncGroupDescription.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = commandUtils.getGroupName(currentAccount, currentGroup);

            if (Objects.equals(groupName, null)) {
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        GROUP_NAME_NOT_FOUND,
                        super.ROWS_COUNT,
                        super.commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
                LOGGER.error(String.format("Error detecting group name of group: %d", currentGroup.getGroupId()));
                return;
            }

            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            SYNC_OK_TG_DESCRIPTION,
                            telegramDataCheck.getChatParameter(currentChannel.getChannelUsername(), "title"),
                            groupName,
                            currentGroup.getSocialMedia().getName(),
                            State.Autoposting.getIdentifier()
                    ),
                    ROWS_COUNT,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION,
                            State.MainMenu.getIdentifier()
                    ),
                    super.ROWS_COUNT,
                    super.commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
