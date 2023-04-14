package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.ok.OKDataCheck;
import polis.util.State;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION = """
            Выбрана группа <b>%s</b> из социальной сети %s.""";
    private static final String NO_VALID_GROUP = """
            Ошибка выбора группы.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и выберите нужную группу.""";
    private final CurrentGroupRepository currentGroupRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final OKDataCheck okDataCheck;
    private final Logger logger = LoggerFactory.getLogger(GroupDescription.class);

    public GroupDescription(String commandIdentifier, String description, CurrentGroupRepository currentGroupRepository,
                            CurrentAccountRepository currentAccountRepository, OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentGroupRepository = currentGroupRepository;
        this.currentAccountRepository = currentAccountRepository;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String groupName = "";
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        switch (currentGroup.getSocialMedia()) {
            case OK -> groupName = okDataCheck.getOKGroupName(currentGroup.getGroupId(),
                    currentAccountRepository.getCurrentAccount(chat.getId()).getAccessToken());
            default -> logger.error(String.format("Social media not found: %s",
                    currentGroup.getSocialMedia()));
        }
        if (currentGroup != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(GROUP_DESCRIPTION, groupName,
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
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
