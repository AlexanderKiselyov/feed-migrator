package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class MainMenu extends Command {
    // TODO добавить описание вариантов дальнейших действий
    private static final String MAIN_MENU = """
            Добро пожаловать в главное меню!
            """;
    public MainMenu(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), MAIN_MENU);
    }
}
