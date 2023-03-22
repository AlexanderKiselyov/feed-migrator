package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;

import java.util.List;

public class MainMenu extends Command {
    // TODO добавить описание вариантов дальнейших действий
    private static final String MAIN_MENU = """
            Добро пожаловать в главное меню!
            """;
    private static final int rowsCount = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.TgChannelsList.getDescription(),
            State.AddTgChannel.getDescription()
    );

    public MainMenu(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), MAIN_MENU, rowsCount,
                commandsForKeyboard, null);
    }
}
