package polis.commands;

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
import polis.datacheck.OkDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.State;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class SyncOkGroupDescription extends Command {
    private static final String SYNC_OK_TG_DESCRIPTION = """
            Телеграм-канал <b>%s</b> и группа <b>%s (%s)</b> были успешно синхронизированы.
            Настроить функцию автопостинга можно по команде /%s.""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION = """
            Невозможно показать информацию по связанным Телеграм-каналу и группе.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private OkDataCheck okDataCheck;

    private final TelegramDataCheck telegramDataCheck;
    private final int rowsCount = 1;
    private final List<String> commandsForKeyboard = List.of(
            State.Autoposting.getDescription()
    );

    public SyncOkGroupDescription() {
        super(State.SyncOkGroupDescription.getIdentifier(), State.SyncOkGroupDescription.getDescription());
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = okDataCheck.getOKGroupName(currentGroup.getGroupId(), currentAccount.getAccessToken());

            if (Objects.equals(groupName, "")) {
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        GROUP_NAME_NOT_FOUND,
                        super.rowsCount,
                        super.commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
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
                    String.format(
                            NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION,
                            State.MainMenu.getIdentifier()
                    ),
                    super.rowsCount,
                    super.commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
