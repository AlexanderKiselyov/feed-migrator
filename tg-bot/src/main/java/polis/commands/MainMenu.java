package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;

import java.util.List;

public class MainMenu extends Command {
    private static final String MAIN_MENU = """
            Добро пожаловать в главное меню!
            Здесь Вы можете посмотреть список добавленных Телеграм-каналов по команде /%s.
            Кроме того, Вы можете добавить новый Телеграм-канал для синхронизации по команде /%s.""";
    private static final int ROWS_COUNT = 2;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.TgChannelsList.getDescription(),
            State.AddTgChannel.getDescription()
    );

    public MainMenu() {
        super(State.MainMenu.getIdentifier(), State.MainMenu.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                String.format(MAIN_MENU, State.AccountsList.getIdentifier(), State.AddTgChannel.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS,
                loggingInfo(user.getUserName()));
    }
}
