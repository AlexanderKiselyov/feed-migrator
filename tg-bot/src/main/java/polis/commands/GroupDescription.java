package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data_check.DataCheck;
import polis.util.State;

import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION = """
            Выбрана группа <b>%s</b> из социальной сети %s.""";
    private static final String NO_VALID_GROUP = """
            Ошибка выбора группы.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и выберите нужную группу.""";

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private DataCheck dataCheck;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupDescription.class);

    public GroupDescription() {
        super(State.GroupDescription.getIdentifier(), State.GroupDescription.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        String groupName = "";
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());

        if (currentGroup != null) {
            switch (currentGroup.getSocialMedia()) {
                case OK -> groupName = dataCheck.getOKGroupName(currentGroup.getGroupId(),
                        currentAccountRepository.getCurrentAccount(chat.getId()).getAccessToken());
                default -> LOGGER.error(String.format("Social media not found: %s",
                        currentGroup.getSocialMedia()));
            }

            if (Objects.equals(groupName, "")) {
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
                return;
            }

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
