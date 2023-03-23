package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.SocialMediaGroup;
import polis.util.State;

import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class GroupDescription extends Command {
    private static final String GROUP_DESCRIPTION = """
            Выбрана группа <b>%s</b> из соцсети %s.""";
    private static final String NO_VALID_GROUP = """
            Ошибка выбора группы.
            Пожалуйста, вернитесь в описание телеграмм канала (/%s) и выберите нужную группу.""";
    private final Map<Long, SocialMediaGroup> currentGroup;

    public GroupDescription(String commandIdentifier, String description, Map<Long, SocialMediaGroup> currentGroup) {
        super(commandIdentifier, description);
        this.currentGroup = currentGroup;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentGroup.get(chat.getId()) != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(GROUP_DESCRIPTION, currentGroup.get(chat.getId()).getName(),
                            currentGroup.get(chat.getId()).getSocialMedia().getName()),
                    rowsCount,
                    commandsForKeyboard,
                    null,null,
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
                    null,null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
