package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class AddOkGroup extends Command {
    private static final String ADD_OK_GROUP = """
            Чтобы добавить новую группу, введите в чат ссылку на нее.
            Примеры ссылок:
            https://ok.ru/ok
            https://ok.ru/group57212027273260""";

    public AddOkGroup(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswer(absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                ADD_OK_GROUP,
                rowsCount,
                commandsForKeyboard,
                null,null,
                GO_BACK_BUTTON_TEXT);
    }
}
