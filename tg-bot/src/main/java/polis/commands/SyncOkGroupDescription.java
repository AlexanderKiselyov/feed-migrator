package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.ok.OKDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.State;

import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class SyncOkGroupDescription extends Command {
    private static final String SYNC_OK_TG_DESCRIPTION = """
            Телеграм-канал <b>%s</b> и группа <b>%s (%s)</b> были успешно синхронизированы.
            Настроить функцию автопостинга можно по команде /%s.""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION = """
            Невозможно показать информацию по связанным Телеграм-каналу и группе.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final CurrentChannelRepository currentChannelRepository;
    private final CurrentGroupRepository currentGroupRepository;
    private final TelegramDataCheck telegramDataCheck;
    private final CurrentAccountRepository currentAccountRepository;
    private final OKDataCheck okDataCheck;
    private final int rowsCount = 1;
    private final List<String> commandsForKeyboard = List.of(
            State.Autoposting.getDescription()
    );

    public SyncOkGroupDescription(String commandIdentifier,
                                  String description,
                                  CurrentChannelRepository currentChannelRepository,
                                  CurrentGroupRepository currentGroupRepository,
                                  CurrentAccountRepository currentAccountRepository,
                                  OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentChannelRepository = currentChannelRepository;
        this.currentGroupRepository = currentGroupRepository;
        this.currentAccountRepository = currentAccountRepository;
        this.okDataCheck = okDataCheck;
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            SYNC_OK_TG_DESCRIPTION,
                            telegramDataCheck.getChatParameter(currentChannel.getChannelUsername(), "title"),
                            okDataCheck.getOKGroupName(currentGroup.getGroupId(), currentAccount.getAccessToken()),
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
